function emailCheck() {
    // 사용자 입력 이메일 값 가져오기
    var email = document.getElementById("email").value;

    // XMLHttpRequest 객체 생성 후 서버에 비동기 요청
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/emailCheck', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify({ email: email }));

    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);

                // 중복 여부에 따라 메시지 출력
                if (response.duplicate) {
                    document.getElementById("emailCheck").innerText = "이미 존재하는 아이디";
                } else {
                    document.getElementById("emailCheck").innerText = "사용할 수 있는 아이디";
                }
            } else {
                console.error('Error occurred: ' + xhr.status);
            }
        }
    };
}
