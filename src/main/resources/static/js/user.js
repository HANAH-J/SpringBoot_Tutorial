// 아이디 입력 체크
const email = document.getElementById("email");

function checkId() {
    if (email.value) {
        email.addEventListener('click', event => {
            function success() {
                let url = "/idCheck" + email.value;
                window.open(url, "_blank1", "toolbar=no, menubar=no, scrollbars=yes, width=450, height=200");
            }
        })
    } else {
        alert('아이디를 입력해 주세요.');
        document.getElementById("email").focus();
    }
}

function useId() {
	// 중복 확인된 아이디를 회원가입 폼에 넣어준다.
	opener.document.memberJoinForm.id.value = document.memberIdCheckForm.id.value;
	// 새로 열린 창 닫기
	self.close();
}