// configure file upload
function selectFile() {
    $('#fileInput').click();
}

function submitUploadForm() {
   var csrfValue = $("#csrfToken").attr("value");
   var file = $('#fileInput').get(0).files[0];
   var formData = new FormData();
   formData.append('file', file);
   $.ajax({
       url: '/upload',
       data: formData,
       headers: {
        'Csrf-Token': csrfValue
       },
       type: 'POST',
       contentType: false,
       processData: false,
       success: function (data) {
         //call your jQuery action here
         $.notify("File upload completed", "success");

       },
       error: function (jqXHR, textStatus, errorThrown) {
         $.notify("error: " + textStatus + ': ' + errorThrown, "error");
       }
    });
    return false;
    }