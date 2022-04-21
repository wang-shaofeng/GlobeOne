javascript: (function () {
    document.getElementById('tbarouter_username').value = 'HACK_mUsername_HACK';
    document.getElementById('tbarouter_password').value = 'HACK_mPassword_HACK';
    console.info('start logging in...');
    Login();
    console.info('on check quick setup...');
    if ($(document.getElementById('MBQuickSetup')))
        btnSkipQSClicked();

    console.info('start sending balance inquiry');
    createMenu(5);

    console.info('start fetching otp');
        var contents = document.getElementById('smsListInfo').getElementsByTagName("tr");
        console.info(contents);
        var i;
        var otp;
        for (i = 0; i < contents.length; i++) {
            console.info(contents[i].getElementsByTagName('td')[1]);
            var regex = new RegExp('[0-9][0-9][0-9][0-9].*is.*your.*Globe.*At.*Home.*app.*v');
            if (regex.exec(contents[i].getElementsByTagName('td')[1].outerText) != null) {
                var message = contents[i].getElementsByTagName('td')[1].outerText.trim();
                otp = message.substring(0, 4);
                break;
            }
        }
        if (otp != null) {
            GlobeAtHome.passOtp(otp)
        } else {
            GlobeAtHome.onFailedToConnect()
        }
})()