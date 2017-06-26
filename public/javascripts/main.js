$('.left .top .add').on('click', function() {
  var jsonData = {sender: "thang", receiver: "pucca"};
  var csrfToken = $("#csrfToken").attr("value");
  $.ajax({
    method: 'post',
    url: '/api/friend/add',
    headers: {'Csrf-Token': csrfToken, 'Content-Type': 'application/json'},
    dataType: 'json',
    data: JSON.stringify(jsonData),
  });
});