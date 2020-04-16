$(function () {
    var headerColor = $('.navbar-brand').data('headercolor');
    // set header
    $('#include_top').css('background-color', headerColor);
    var primaryColor = $('.navbar-brand').data('primarycolor');
    $('.btn-primary').css('background-color', primaryColor);
    $('.btn-primary').css('border-color', primaryColor);
});