javascript: (function () {
 $('#login_username').val('HACK_userId_HACK');
 $('#login_password').val('HACK_password_HACK');
 $('#login_password_text').val('HACK_password_HACK');
 EMUI.LoginObjController.Login(1);

  setTimeout(function(){
    $('#privacyAgreen').click();
    setTimeout(function(){
        $('#privacyAgreen').click();
        setTimeout(function(){
            EMUI.GuideController.wifiNext();
            setTimeout(function(){
                $('#new_password').val('HACK_password_HACK');
                EMUI.checkUserController.checkCurrentPAssword();
            }, 3000);
        }, 3000);
    }, 3000);
  }, 3000);
})();
