javascript: (function () {
    console.log('Start get otp');
    sms_initSMS();
    setTimeout(function () {
        console.log('len:' + g_sms_smsList.length);
        smsIndex = 0;
        var smsContent = '';
        var smsPhone = '';
        for (var i = 0; i < g_sms_smsList.length; i++) {
            var content = g_sms_smsList[i].Content;
            console.log(g_sms_smsList[i].Phone);
            console.log(g_sms_smsList[i].Content);
            if (content.indexOf('Your One-time PIN is') > 0) {
                console.log('content:' + content);
                smsIndex = g_sms_smsList[i].Index;
                smsContent = g_sms_smsList[i].Content;
                smsPhone = g_sms_smsList[i].Phone;
                break;
            }
        }
        GlobeAtHome.passOtp(smsContent.substr(21, 27));
    }, 5000);
})()