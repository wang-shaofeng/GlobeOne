javascript: (function () {
    createMenu(5);
    console.info('start fetching otp');
    var contents = document.getElementById('smsListInfo').getElementsByTagName("tr");
    console.info(contents);
    var i;
    var otp;
    for (i = 0; i < contents.length; i++) {
        console.info(contents[i].getElementsByTagName('td')[1]);
        var regex = new RegExp("Your.*One-time.*PIN.*is.*[0-9][0-9][0-9][0-9][0-9][0-9]");
        if (regex.exec(contents[i].getElementsByTagName('td')[1].outerText) != null) {
            var message = contents[i].getElementsByTagName('td')[1].outerText.trim();
            otp = message.substring(message.length - 6);
            break;
        }
    }
    if (otp != null) {
        GlobeAtHome.passOtp(otp)
    } else {
        GlobeAtHome.onFailedToConnect()
    }
})()