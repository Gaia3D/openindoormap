/**
 * 시계열
 * @since 20200827 init
 */
let TimeSeriesObj = function () {
    this.datas = [];
    this.index = 0;

    //
    this.interval = null;
};


/**
 * 초기
 * */
TimeSeriesObj.prototype.init = function () {
    let div = this.createElements();

    //
    MAGO3D_INSTANCE.getMagoManager().overlayContainer.appendChild(div);

    //
    this.setEventHandler();

    //일자 데이터 바인드
    let _deDatas = function () {
        let arr = [];
        arr.push({ 'value': '20200301', 'text': '20200301' });
        arr.push({ 'value': '20201111', 'text': '20201111' });
        arr.push({ 'value': '20201212', 'text': '20201212' });

        //
        return arr;
    };
    //
    this.bindDeDatas(_deDatas());

    //인터벌 데이터 바인드
    let _intervalDatas = function () {
        let arr = [];
        arr.push({ 'value': '3', 'text': '3초' });
        arr.push({ 'value': '5', 'text': '5초' });
        arr.push({ 'value': '10', 'text': '10초' });

        //
        return arr;
    };
    //
    this.bindIntervalDatas(_intervalDatas());
};



/**
 * 이벤트 핸들러 등록
 * */
TimeSeriesObj.prototype.setEventHandler = function () {
    let the = this;

    //이전
    document.querySelector('.ds-prev').addEventListener('click', function () {
        let json = the.prev();
    });

    //정지
    document.querySelector('.ds-stop').addEventListener('click', function () {
        the.stop();
    });

    //재생
    document.querySelector('.ds-play').addEventListener('click', function () {
        the.play();
    });

    //다음
    document.querySelector('.ds-next').addEventListener('click', function () {
        let json = the.next();
    });

    //선택박스 change
    document.querySelector('.ds-de').addEventListener('change', function () {
        let index = this.selectedIndex;
        let json = the.getDataByIndex(index);

        //
        the.setIndex(index);
        the.playData(json);
    });
};



/**
 * 데이터 바인드
 * @param {any} arr
 */
TimeSeriesObj.prototype.bindDeDatas = function (arr) {
    //
    this.datas = arr;
    this.index = 0;

    let select = document.querySelector('.ds-de');
    if (Pp.isNull(select)) {
        return;
    }

    //
    for (let i = 0; i < arr.length; i++) {
        let d = arr[i];

        //
        let option = document.createElement('option');
        option.value = d.value;
        option.text = d.text;

        //
        select.appendChild(option);
    }

    //
    select.selectedIndex = 0;

    //
    return select;
};


/**
 * 인터벌 데이터 바인드
 * @param {any} arr
 */
TimeSeriesObj.prototype.bindIntervalDatas = function (arr) {
    let select = document.querySelector('.ds-interval');
    if (Pp.isNull(select)) {
        return;
    }

    //
    for (let i = 0; i < arr.length; i++) {
        let d = arr[i];

        //
        let option = document.createElement('option');
        option.value = d.value;
        option.text = d.text;

        //
        select.appendChild(option);
    }

    //
    select.selectedIndex = 0;

    //
    return select;
};


/**
 * 엘리먼트 생성
 * @returns {Element} 생성된 엘리먼트
 * */
TimeSeriesObj.prototype.createElements = function () {

    //
    let _div = function () {
        let div = document.createElement('div');
        div.style.backgroundColor = '#efefef';
        div.style.position = 'absolute';
        div.style.zIndex = '999';
        div.style.width = '200px';
        div.style.height = '200px';
        div.style.bottom = '0px';
        div.style.float = 'bottom';
        div.className = 'ds-time-series mago3d-overlayContainer-defaultControl';
        div.style.pointerEvents = 'auto';

        //
        return div;
    };
    //
    let div = _div();


    //이전
    let _preButton = function () {
        let preButton = document.createElement('button');
        preButton.innerHTML = '<';
        preButton.className = 'ds-prev textBtnSub';

        //
        return preButton;
    }
    //
    div.appendChild(_preButton());


    //일시정지
    let _pauseButton = function () {
        let pauseButton = document.createElement('button');
        pauseButton.innerHTML = '||';
        pauseButton.className = 'ds-stop textBtnSub';


        //
        return pauseButton;
    }
    //
    div.appendChild(_pauseButton());


    //재생
    let _playButton = function () {
        let playButton = document.createElement('button');
        playButton.innerHTML = '▷';
        playButton.className = 'ds-play textBtnSub';


        //
        return playButton;
    };
    //
    div.appendChild(_playButton());


    //다음
    let _nextButton = function () {
        let nextButton = document.createElement('button');
        nextButton.innerHTML = '>';
        nextButton.className = 'ds-next textBtnSub';

        //
        return nextButton;
    };
    //
    div.appendChild(_nextButton());


    //타임시리즈
    let _select = function () {
        let select = document.createElement('select');
        select.className = 'ds-de';
        select.style.width = '100%';

        //
        return select;
    };
    //
    div.appendChild(_select());


    //인터벌
    let _interval = function () {
        let select = document.createElement('select');
        select.className = 'ds-interval';
        select.style.width = '100%';

        //
        return select;
    };
    //
    div.appendChild(_interval());

    //
    return div;
};


/**
 * 데이터 조회. this.index의 값은 변경되지 않음
 * @param {string} gbn 0|curr:현재, -1|prev:이전, 1|next:다음, 'first':처음, 'last':마지막
 */
TimeSeriesObj.prototype.getData = function (gbn) {
    if ('0' === gbn || 'curr' === gbn) {
        return this.getDataByIndex(this.index);
    }

    //
    if ('-1' === gbn || 'prev' === gbn) {
        if (0 > (this.index - 1)) {
            return {};
        }

        //
        return this.getDataByIndex(this.index - 1);
    }

    //
    if ('1' === gbn || 'next' === gbn) {
        if (this.datas.length <= (this.index + 1)) {
            return {};
        }

        //
        return this.getDataByIndex(this.index + 1);
    }

    //
    if ('first' === gbn) {
        return this.getDataByIndex(0);
    }

    //
    if ('last' === gbn) {
        return this.getDataByIndex(this.datas.length - 1);
    }
};


/**
 * this.index의 값은 변경되지 않음
 * 
 * @param {any} index
 */
TimeSeriesObj.prototype.getDataByIndex = function (index) {
    let json = this.datas[index];
    json.index = index;

    //
    return json;
}


TimeSeriesObj.prototype.setIndex = function (index) {
    this.index = index;
};



/**
 * 인터벌 데이터 조회
 * @returns {number} 인터벌 값(초)
 * */
TimeSeriesObj.prototype.getIntervalValue = function () {
    return parseInt(document.querySelector('.ds-interval').value);
};


/**
 * 이전 데이터 조회. this.index값 변경
 * */
TimeSeriesObj.prototype.movePrev = function () {
    this.index--;

    if (0 > this.index) {
        this.index = 0;
    }

    //
    return this.getDataByIndex(this.index);
};


/**
 * 다음 값 조회. this.index값 변경
 * */
TimeSeriesObj.prototype.moveNext = function () {
    this.index++;

    if (this.datas.length <= this.index) {
        this.index = this.datas.length - 1;
    }

    //
    return this.getDataByIndex(this.index);
};



/**
 * 이전 데이터 처리
 * */
TimeSeriesObj.prototype.prev = function () {
    //
    let json = this.movePrev();
    this.playData(json);

    //
    return json;
};


/**
 * 정지
 * */
TimeSeriesObj.prototype.stop = function () {
    clearInterval(this.interval);

    //
    console.log('stop');
};


/**
 * (자동)재생
 * */
TimeSeriesObj.prototype.play = function () {
    this.stop();


    //
    this.playData(this.getData('curr'));
    //
    let the = this;
    this.interval = setInterval(function () {
        the.playData(the.moveNext());


    }, this.getIntervalValue() * 1000);
};



/**
 * 다음 데이터 처리
 * */
TimeSeriesObj.prototype.next = function () {
    let json = this.moveNext();
    this.playData(json);

    //
    return json;
};


/**
 * 데이터 처리
 * @param {any} json
 */
TimeSeriesObj.prototype.playData = function (json) {
    //TODO ui의 값 변경
    document.querySelector('.ds-de').selectedIndex = json.index;


    //TODO
    console.log(new Date(), json);

    //마지막 데이터이면
    if (json.index === this.getData('last').index) {
        this.stop();
    }
};

//
let tsobj = new TimeSeriesObj();