$(function () {

    "use strict";

    $('#btnSave').on('click', function(e) {

        $('#frmProp').submit();
    });

    $('#btnClearCache').on('click', function(e) {

        var groupName = $(this).data('groupname');

        $('#frmProp').attr('method', 'delete');
        $('#frmProp').attr('action', '/cfg/configs/'+groupName+'/caches');
        $('#frmProp').submit();
    });

});