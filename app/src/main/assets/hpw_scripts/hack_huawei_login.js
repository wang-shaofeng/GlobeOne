javascript: (function () {
     console.log($('#tooltip_sms'));
     showloginDialog();
     var username = document.getElementById('username');
     username.value = 'HACK_userId_HACK';
     var password = document.getElementById('password');
     password.value = 'HACK_password_HACK';
     login('smsinbox.html')
})()