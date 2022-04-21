javascript: (function () {
    function segueToSms(second) {
        setTimeout(function () {
            var items = document.getElementById('items');
            if (items.getElementsByClassName('w_156')[3] != null) {
                tosms('#sms')
            } else {
                segueToSms(second)
            }
        }, second + 200)
    }
    segueToSms(1000);
})()