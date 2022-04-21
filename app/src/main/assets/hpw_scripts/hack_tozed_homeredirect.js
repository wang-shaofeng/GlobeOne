javascript: (function () {
    function segueToSms(second) {
        setTimeout(function () {
            var items = document.getElementById('items');
            if (items.getElementsByClassName('germanFont')[3] != null) {
                items.getElementsByClassName('germanFont')[3].click()
            } else {
                segueToSms(second)
            }
        }, second + 200)
    }
    segueToSms(1000);
})()