$(document).ready(function() {

    $('.btn-lock-apiclient').on('click', function(e) {

        var clientId = $(this).data('clientid');

        swal({
            title: 'Lock',
            text: 'Are you sure you want to lock this API Client? This will blocked all API request using this Client Id',
            type: 'warning',
            showCancelButton: true,
            confirmButtonClass: 'btn-danger',
            confirmButtonText: 'Lock'
        }).then((result) => {
            if (result.value) {
                window.location.replace('/apc/clients/'+clientId+'/lock');
            }
        });
    });

    $('.btn-unlock-apiclient').on('click', function(e) {

        var clientId = $(this).data('clientid');

        swal({
            title: 'Unlock',
            text: 'Are you sure you want to unlock this API Client?',
            type: 'warning',
            showCancelButton: true,
            confirmButtonClass: 'btn-danger',
            confirmButtonText: 'Unlock'
        }).then((result) => {
            if (result.value) {
                window.location.replace('/apc/clients/'+clientId+'/unlock');
            }
        });
    });

    $('.btn-delete-apiclient').on('click', function(e) {

        var clientId = $(this).data('clientid');

        swal({
            title: 'Delete',
            text: 'Are you sure you want to delete this API Client? This action is irreversible!. All API request using this Client Id will be forever blocked',
            type: 'error',
            showCancelButton: true,
            confirmButtonClass: 'btn-danger',
            confirmButtonText: 'Delete'
        }).then((result) => {
            if (result.value) {
                window.location.replace('/apc/clients/'+clientId+'/delete');
            }
        });
    });
});