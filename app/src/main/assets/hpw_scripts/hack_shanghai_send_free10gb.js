javascript: (function () {
    console.info('start sending free10gb');
    createMenu(5);
    var smsMap = new Map();
    smsMap.put("RGW/sms_info/sms/id", -1);
    smsMap.put("RGW/sms_info/sms/gsm7", 1);
    smsMap.put("RGW/sms_info/sms/address", "8080,");
    smsMap.put("RGW/sms_info/sms/body", UniEncode("FREE10GB"));
    smsMap.put("RGW/sms_info/sms/date", GetSmsTime());
    smsMap.put("RGW/sms_info/sms/protocol", 0);
    PostXml("sms", "sms.send", smsMap);
    createMenu(5);
    GlobeAtHome.onFinishSendFree10GBSms()
})()