$(document).ready(function() {
    $('#btnSendToPrint').on('click', function () {
        var printUrl = $(this).data('pdfurl');
        printJS({
            printable: printUrl,
            type: 'pdf',
            showModal: true,
            onLoadingEnd: onLoadingEnd
        });
    });

    var onLoadingEnd = function () {
        $('#printJS').hide();
    };
});