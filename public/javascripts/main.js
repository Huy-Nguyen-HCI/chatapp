function init() {

    /* Make chat area responsive to window height */
    var height =
        window.innerHeight - parseFloat($('.navbar').height()) -
        parseFloat($('.message-write').height()) - parseFloat($('.new-message-head').height()) - 50;
    $('.chat-area').css('height', height + 'px');

    var mathInputBox = $('#mathquill');
    var textInputBox = $('#input-box');

    /* Set up math input */
    mathInputBox.hide();
    closeKeyboard();

    $('#typeMath').change( function() {
        if (this.checked) {
            mathInputBox.css('display', 'inline-block');
            textInputBox.hide();
        }
        else {
            mathInputBox.hide();
            textInputBox.css('display', 'inline-block');
            closeKeyboard();
        }
    });

    mathInputBox.click(function (e) {
        setTimeout(function() {
            openKeyboard();
        }, 100);
    });

    mathInputBox.keypress(function (e) {
        var keycode = (event.keyCode ? event.keyCode : event.which);
        if (keycode == '13') {
            e.preventDefault();
            $('#mathquill-submit').click();
        }
    })

    /* tell MathJax to recognize inline math by $ $ */
    MathJax.Hub.Config({
        tex2jax: {inlineMath: [["$","$"]]}
    });

    /* Set chatroom switching */
    $('.chat-area[data-chat=room1]').addClass('active-chat');
    $('.member-list li[data-chat=room1]').addClass('active');
    $('.member-list li').mousedown(function(){
        if ($(this).hasClass('.active')) {
            return false;
        } else {
            var findChat = $(this).attr('data-chat');
            $('.chat-area').removeClass('active-chat');
            $('.member-list li').removeClass('active');
            $(this).addClass('active');
            $('.chat-area[data-chat = '+findChat+']').addClass('active-chat');
        }
    });
    
    

    /* File upload */
    $('#fileInput').on("click", function () {
        console.log("changed");
        $(this).closest('form').submit();
    });
}

function setupMathInput(mathField) {
    $('#keyboard .key').click( function(event) {
        event.preventDefault();

        if (mathField)  {
            if ($(this).data('action') == 'write') {
                mathField.write($(this).data('content'));
            } else if($(this).data('action') == 'cmd') {
                mathField.cmd($(this).data('content'));
            } else if($(this).data('action') == 'keystroke') {
                mathField.keystroke($(this).data('content'));
            } else if($(this).data('action') == 'switch-keys') {
                $(this).parents('#keyboard').switchClass("trig", $(this).data("content"));
                $(this).parents('#keyboard').switchClass("std", $(this).data("content"));
            } else if($(this).data('action') == 'keyboard-hide'){
                closeKeyboard();
            }

            if (typeof $(this).data('stepback') !== 'undefined') {
                for (var i = 0; i < parseInt($(this).data('stepback')); i++) {
                    mathField.keystroke('Left');
                }
            }

            if (typeof $(this).data('stepforward') !== 'undefined') {
                for (var i = 0; i < parseInt($(this).data('stepforward')); i++) {
                    mathField.keystroke('Right');
                }
            }

            mathField.focus();
        }
    });

    $('#keyboard-mask').height($('#keyboard-wrapper').height());
}

function closeKeyboard() {
    $('#keyboard-mask').slideUp();
    $('#keyboard-wrapper').slideUp();
}

function openKeyboard() {
    $('#keyboard-wrapper').slideDown();
    $('#keyboard-mask').slideDown("fast", function() {
        $(window).scrollTop($('#keyboard-mask').position().top + $('#keyboard-mask').outerHeight() + 30);
    });
}
