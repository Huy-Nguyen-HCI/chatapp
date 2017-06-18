function init(webSocketURL) {

  var connection = new WebSocket(webSocketURL),
      inputBox = $('#input-box'),
      messages = $('#message-area');

  var send = function() {
      var text = inputBox.val();
      inputBox.val("");
      connection.send(text);
  };

  connection.onopen = function () {
      $('.write').keypress(function(event){
          var keycode = (event.keyCode ? event.keyCode : event.which);
          if(keycode == '13'){
              send();
          }
      });
  };
  connection.onerror = function (error) {
      console.log('WebSocket Error ', error);
  };
  connection.onmessage = function (event) {
      messages.append($(`<div class='bubble you'>${event.data}</div>`))
  }
}


$('.chat[data-chat=person2]').addClass('active-chat');
$('.person[data-chat=person2]').addClass('active');

$('.left .person').mousedown(function(){
    if ($(this).hasClass('.active')) {
        return false;
    } else {
        var findChat = $(this).attr('data-chat');
        var personName = $(this).find('.name').text();
        $('.right .top .name').html(personName);
        $('.chat').removeClass('active-chat');
        $('.left .person').removeClass('active');
        $(this).addClass('active');
        $('.chat[data-chat = '+findChat+']').addClass('active-chat');
    }
});

$('.add').click(function() {
  var username = prompt("Enter the username of your friend:")
  if (username && username.length > 0) {
   $.post("/add-friend", {'targetUser' : username}, function(data) {
     go(500);
     $.notify("Friend request successfully sent.", "success");
   });
  }
  else {
    $.notify("Username cannot be empty.", "error")
  }
})