javascript: (function () {
    console.log('Start get otp');
     setTimeout(function () {
        var smsPhoneList = document.querySelectorAll("[id^=sms_list_contract_item_number_]");
        var smsContentList = document.querySelectorAll("[id^=sms_list_contract_item_content_]");
        var  smsPhone = "";
        var  smsContent = "";

        for(var i = 0; i < smsPhoneList.length; i++) {
            try{
                  var cons = smsContentList[i].innerHTML;
                  if (cons.includes('Your One-time PIN is')) {
                      smsPhone = smsPhoneList[i].innerHTML;
                      smsContent = smsContentList[i].innerHTML;
                      break;
                  }
            }catch { }
        }
         GlobeAtHome.passOtp(smsContent.substring(21, 27));
    }, 4000);
})()