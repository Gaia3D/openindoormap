const Pagination = function(pageNo, count, top, nextLink) {

    // 페이지 번호
    this.pageNo = pageNo;
    // 총 건수
    this.totalCount = count;
    // 한 페이지에 표시할 건수
    this.pageRows = top;
    // 다음 페이지 링크
    this.nextLink = nextLink;
    // pageNo를 몇개까지 표시하고 다음 페이지로 넘길건지( < 1 2 3 4 5 6 7 > 이 경우 7이 됨)
    this.pageListCount = 5;

    // 게시물 번호
    this.rowNumber;
    // 처음
    this.firstPage = 1;
    // 끝
    this.lastPage;
    // 페이지 시작
    this.startPage;
    // 페이지 종료
    this.endPage;
    // 이전
    this.prePageNo;
    // 다음
    this.nextPageNo;
    // 이전 페이지 존재여부 Flag
    this.existPrePage = false;
    // 다음 페이지 존재여부 Flag
    this.existNextPage = false;
    // 페이지 처리를 위한 시작
    this.offset;

    this.init();
}
Pagination.prototype.init = function() {
    this.rowNumber = this.totalCount - (this.pageNo - 1) * this.pageRows;

    this.offset = (this.pageNo - 1) * this.pageRows;

    this.lastPage = 0;
    if(this.totalCount != 0) {
        if(this.totalCount % this.pageRows == 0) {
            this.lastPage = Math.floor(this.totalCount / this.pageRows);
        } else {
            this.lastPage = Math.floor(this.totalCount / this.pageRows) + 1;
        }
    }

    this.startPage = Math.floor((this.pageNo - 1) / this.pageListCount) * this.pageListCount + 1;
    this.endPage = Math.floor((this.pageNo - 1) / this.pageListCount) * this.pageListCount + this.pageListCount;
    if(this.endPage > this.lastPage) {
        this.endPage = this.lastPage;
    }

    let remainder;
    this.prePageNo = 0;
    if(this.pageNo > this.pageListCount) {
        // TODO 이전을 눌렀을때 현재 페이지 - 10 이 아닌 항상 1, 11, 21... 형태로 표시하고 싶을때
        remainder = this.pageNo % this.pageListCount;
        this.prePageNo = this.pageNo - this.pageListCount - remainder + 1;
        // TODO 이전을 눌렀을때 현재 페이지 - 10 형태로 표시하고 싶을경우 3, 13, 23 ...
//			this.prePageNo = this.pageNo - this.pageListCount;
        this.existPrePage = true;
    }

    this.nextPageNo = 0;
    if(this.lastPage > this.pageListCount && this.pageNo <= ((this.lastPage / this.pageListCount) * this.pageListCount)) {
        if(this.lastPage >= (this.startPage + this.pageListCount)) {
            if(this.pageNo % this.pageListCount == 0) {
                // TODO 다음을 눌렀을때 현재 페이지 + 10 이 아닌 항상 11, 21, 31... 형태로 표시하고 싶을때
                this.nextPageNo = this.pageNo + 1;
            } else {
                // TODO 다음을 눌렀을때 현재 페이지 + 10 이 아닌 항상 11, 21, 31... 형태로 표시하고 싶을때
                if(this.lastPage >= this.pageNo + this.pageListCount) {
                    remainder = this.pageNo % this.pageListCount;
                    this.nextPageNo = this.pageNo + this.pageListCount - remainder + 1;
                    // TODO 다음을 눌렀을때 현재 페이지 + 10 형태로 표시하고 싶을경우 13, 23, 33 ...
                    //					this.nextPageNo = this.pageNo + this.pageListCount;
                } else {
                    remainder = this.lastPage % this.pageListCount;
                    this.nextPageNo = this.lastPage - remainder + 1;
                    // TODO 다음을 눌렀을때 현재 페이지 + 10 형태로 표시하고 싶을경우 13, 23, 33 ...
                    //					this.nextPageNo = this.lastPage;
                }
            }
            this.existNextPage = true;
        }
    }

    if(this.totalCount == 0) {
        this.pageNo = 0;
    }
};