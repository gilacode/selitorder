$(document).ready(function() {

    $('#signupForm').on('submit', function(e) {
        var password = $('#password').val();
        var confirmPassword = $('#repeatPassword').val();
        if(password!==confirmPassword) {
            $('#instruction').html("Password does not match");
            e.preventDefault();
            return false;
        }
    });

    $('#changePwdForm').on('submit', function(e) {
        var password = $('#password').val();
        var confirmPassword = $('#confirmPassword').val();
        if(password!==confirmPassword) {
            $('#instruction').html("Password does not match");
            e.preventDefault();
            return false;
        }
    });

    $('#btnFilterUsers').on('click', function(e) {

        var filterUrl = $(this).data('filterurl');

        var status = $('#selectStatus').val();
        var type = $('#selectType').val();

        window.location.href = filterUrl+'?status='+status+'&type='+type;
    });

    $('#makeAdminForm input').keydown(function(e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            return false;
        }
    });

    $('#btnMakeAdmin').on('click', function(e) {

        swal({
            title: 'Make Admin',
            text: 'Are you sure you want to upgrade this user with Super Admin privileges?',
            type: 'warning',
            showCancelButton: true,
            confirmButtonClass: 'btn-danger',
            confirmButtonText: 'Upgrade'
        }).then((result) => {

            $('#makeAdminForm').submit();
        });

    });

    $('#downgradeAdminForm input').keydown(function(e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            return false;
        }
    });

    $('#downgradeAdmin').on('click', function(e) {

        swal({
            title: 'Downgrade Admin',
            text: 'Are you sure you want to downgrade this user from Super Admin privileges?',
            type: 'warning',
            showCancelButton: true,
            confirmButtonClass: 'btn-danger',
            confirmButtonText: 'Downgrade'
        }).then((result) => {

            $('#downgradeAdminForm').submit();
        });

    });
});