$(function () {

    "use strict";

    $('.onlyNumber').on('keydown', function(e){-1!==$.inArray(e.keyCode,[46,8,9,27,13,110,190])||(/65|67|86|88/.test(e.keyCode)&&(e.ctrlKey===true||e.metaKey===true))&&(!0===e.ctrlKey||!0===e.metaKey)||35<=e.keyCode&&40>=e.keyCode||(e.shiftKey||48>e.keyCode||57<e.keyCode)&&(96>e.keyCode||105<e.keyCode)&&e.preventDefault()});

    $('.appcal').datepicker({
        format: 'dd/mm/yyyy',
        autoclose: true,
        todayHighlight: true,
        orientation: "auto",
        enableOnReadonly: false
    });

    $('.appyear').datepicker({
        format: 'yyyy',
        minViewMode: 2,
        autoclose: true,
        todayHighlight: true,
        orientation: "auto",
        enableOnReadonly: false
    });

    $('.appmonth').datepicker({
        format: 'mm',
        minViewMode: 1,
        autoclose: true,
        todayHighlight: true,
        orientation: "auto",
        enableOnReadonly: false
    });

    $('.apptime').datetimepicker({
        format: 'HH:mm',
        icons: {
            up: 'fa fa-chevron-up',
            down: 'fa fa-chevron-down',
        }
    });

    $('.appcurrency').each(function(){
        $(this).inputmask("numeric",{
            radixPoint: ".",
            groupSeparator: ",",
            digits: 0,
            autoGroup: true,
            prefix: ' Rp ',
            rightAlign: true,
            removeMaskOnSubmit: true,
            clearMaskOnLostFocus: true,
            autoUnmask: true
        });
    });

    $('input').ready(function(){
        var isDefault = $(this).hasClass('default-case');
        if(!isDefault && this.value){
            this.value = this.value.toUpperCase();
        }
    });

    $('input').keyup(function(){
        var isDefault = $(this).hasClass('default-case');
        if(!isDefault && this.value){
            this.value = this.value.toUpperCase();
        }
    });

    $('table.dataTable').DataTable({
        responsive: true,
        columnDefs: [
            { responsivePriority: 1, targets: 0 },
            { responsivePriority: 2, targets: -1 }
        ],
        deferRender: true
    });

    // automatically bind any select HTML element with "select2" class as Select2 component
    $('select.select2').each(function (index) {
        $(this).select2({
            width: '100%',
            allowClear: true,
            placeholder: {
                id: '-1', // the value of the option
                text: 'Please select'
            },
            language: "en"
        });
    });

    $('input[required]').parents('.form-group').find('label').append('<span class="red">*</span>');
    $('select[required]').parents('.form-group').find('label').append('<span class="red">*</span>');
    $('textarea[required]').parents('.form-group').find('label').append('<span class="red">*</span>');

    $('#backButton').on('click', function() {
        window.history.back();
    });

    var notifications = function() {

        $('#notificationContainer').nextAll().remove();

        var username = $('#notificationContainer').data('username');

        if(username) {

            $.getJSON('/api/users/'+username+'/notifs/category/WEB?onlyNew=true', function(notifications) {

                var divNav = $('<div>', { class: 'notify' });
                var spanNavHeart = $('<span>', { class: 'heartbit' });
                var spanNavPoint = $('<span>', { class: 'point' });
                divNav.append(spanNavHeart);
                divNav.append(spanNavPoint);

                $('#notificationEnvelope').after(divNav);

                $.each(notifications, function(i, notification) {

                    var line = $('<li>');

                    var messageCenter = $('<div>', { class : 'message-center'});

                    var anchor = $('<a>');
                    if(notification.redirectUrl) {
                        anchor.attr('href', notification.redirectUrl);
                    } else {
                        anchor.attr('href', '/not/detail/'+notification.id);
                    }

                    var content = $('<div>', { class: 'mail-contnet' });
                    var title = $('<h5>').text(notification.channelDesc);
                    var topic = $('<span>', { class: 'mail-desc' }).text(notification.topicSubject);
                    var time = $('<span>', { class: 'time' }).text(notification.createdAtFormatted);

                    content.append(title);
                    content.append(topic);
                    content.append(time);

                    anchor.append(content);

                    messageCenter.append(anchor);

                    line.append(messageCenter);

                    $('#notificationContainer').after(line);
                });
            });
        }
    };

    notifications();
    setInterval(notifications, 10000);
});