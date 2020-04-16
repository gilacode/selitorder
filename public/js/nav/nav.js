$(function () {

    $('#selectTemplate').on('change', function(e) {
        var templateName = $('#selectTemplate').val();
        if(templateName) {
            window.location.href = '/nav/templates/'+templateName;
        }
    });

    $('#btnCopyTmpl').on('click', function(e) {
        var username = $('#selectedUser').val();
        var templateName = $('#copyTemplate').val();
        if(templateName) {
            window.location.href = '/nav/users/'+username+'/templates/'+templateName;
        }
    });

});