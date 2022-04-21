javascript: (function () {
    console.log('Start sending free10gb');
    document.getElementById('message').click();
    setTimeout(function () {
        document.getElementById('recipients_number').value = '8080';
        document.getElementById('message_content').value = 'FREE10GB';
        document.getElementById('pop_send').click();
        setTimeout(function () {
            GlobeAtHome.onFinishSendFree10GBSms()
        }, 10000);
    }, 2000);
})()