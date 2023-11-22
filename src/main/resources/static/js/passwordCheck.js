function passwordCheck() {
    var password = document.getElementsByName("password")[0].value;
    var matchPassword = document.getElementsByName("rePw")[0];
    var message = document.getElementById("passwordCheckMessage");

    if (password === matchPassword.value) {
        message.innerHTML = "비밀번호가 일치합니다.";
        message.style.color = "green";
    } else {
        message.innerHTML = "비밀번호가 일치하지 않습니다.";
        message.style.color = "red";
    }
}
