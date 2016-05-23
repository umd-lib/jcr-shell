jsshell = {
    path: '/'
};
jsshell.out = {
    print: function(txt) {
        $('#output').append('<pre>' + txt + '</pre>');
    },
    append: function(html) {
        $('#output').append(html);
    }
};
jsshell.scroll = function() {
    var dh = document.body.scrollHeight;
    var ch = document.body.clientHeight;
    if (dh > ch) {
        window.scrollTo(0, dh - ch);
    }
};
jsshell.complete = function(val, match, suggest, fail) {
    var matched = null;
    $.get('rest/complete' + jsshell.path,
        { current: val },
        function(response) {
            var start = val.substr(0, response.start);
            var candidates = response.candidates;
            if (candidates.length == 1) {
               match(start, candidates[0]);
            } else if (candidates.length > 1) {
                suggest(start, candidates);
            } else {
                fail();
            }
        }
    ).error(function() {
        fail();
    });
};
jsshell.execute = function(val, success, fail) {
    $.get('rest/execute' + jsshell.path,
        { command: val },
        function(execution) {
            jsshell.out.print('<b>jcr-shell: > ' + val + '</b>');
            jsshell.path = execution.path;
            if (execution.messages != undefined) {
                $.each(execution.messages, function(index, message) {
                    if (message.type == 'LINE') {
                        var text = '';
                        $.each(message.parts, function(index, part) {
                            switch (part.mode) {
                            case 'PLAIN':
                                text = text + part.text;
                                break;
                            case 'DEBUG':
                                text = text + '<span class="debug">' + part.text + '</span>'
                                break;
                            case 'OK':
                                text = text + '<span class="ok">' + part.text + '</span>'
                                break;
                            case 'WARN':
                                text = text + '<span class="warn">' + part.text + '</span>'
                                break;
                            case 'ERROR':
                                text = text + '<span class="error">' + part.text + '</span>'
                                break;
                            }
                        });
                        jsshell.out.print(text);
                    } else if (message.type == 'TABLE') {
                        var headers = message.headers;
                        var text = '<table>';

                        text += '<tr>';
                        $.each(headers, function(index, header) {
                            text += '<th><tt>' + header + '</tt></th>';
                        });
                        text += '</tr>\n';

                        $.each(message.rows, function(index, row) {
                            text += '<tr>';
                            $.each(row, function(index, value) {
                                text += '<td><tt>' + value + '</tt></td>';
                            });
                            text += '</tr>';
                        });

                        text += '</table>';
                        jsshell.out.append(text);
                    }
                });
            }
            success();
        }
    ).error(function() {
        fail();
    });
};
jsshell.util = {
    // find the greatest common start string by a binary search
    common : function(a, b) {
        var min = a.length < b.length ? a.length : b.length;
        if (min == 0) {
            return '';
        }
        if (a == b.substr(0, min)) {
            return a;
        } else if (min == 1) {
            return '';
        }
        var half = (min + 1) / 2;
        if (a.substr(0, half) == b.substr(0, half)) {
            return a.substr(0, half) + jsshell.util.common(a.substr(half, min - half), b.substr(half, min - half));
        } else {
            return jsshell.util.common(a.substr(0, half), b.substr(0, half));
        }
    }
};

$(document).keydown(function(e) {
    if (e.keyCode == 9) {
        e.preventDefault();
        e.stopPropagation();
        $('#prompt').trigger('tab');
    }
});
$(document).keyup(function(e) {
    if (e.keyCode == 9) {
        e.preventDefault();
        e.stopPropagation();
    }
});
$(document).ready(function() {
    $('#prompt').bind('tab', function(e) {
        this.disabled = true;
        var el = this;
        var done = function() {
            jsshell.scroll();
            el.disabled = false;
            el.focus();
        };

        $('#candidates').empty();

        jsshell.complete($(this).val(),
        function(start, suggestion) {
            $(el).val(start + suggestion);
            done();
        }, function(start, candidates) {
            var shared = candidates[0];
            $.each(candidates, function(i, item) {
                shared = jsshell.util.common(shared, item);
            });
            $(el).val(start + shared);
            $.each(candidates, function(i, item) {
                $('#candidates').append("<li>" + item + "</li>\n");
            });
            done();
        }, done);
    });
    $('#prompt').bind('change', function(e) {
        var command = $(this).val();
        var el = this;
        $('#candidates').empty();
        jsshell.execute(command, function() {
            $(el).val("");
            jsshell.scroll();
        }, function() {
            console.log('fail');
            jsshell.scroll();
        });
    });
    $('#prompt').focus();
});
