//층별 높이(m)
const HEIGHT_PER_FLOOR = 3.3;
//
const IP = 'localhost';
//경관 비교
const	LS_DIFF_REST_URL = 'http://118.42.112.206:5891/adminsvc/ls-diff-rest';
//경관 점
const	LS_POINT_REST_URL = 'http://118.42.112.206:5891/adminsvc/ls-point-rest';
//
const TERRAIN_URL = 'http://118.42.112.206:9997/tilesets/terrain/';



/**
 * 업무 공통 js
 * @author	gravity
 * @since	20200824	init
 */
 let DS = function(){
 	
 };

DS.init = function(){
	toastr.options = {
		"positionClass": "toast-top-center"
	}
};

 
 
 /**
  * 페이징 관련 처리
  * 	1. 페이징 계산
  * 	2. 계산 결과로 화면에 페이징 관련 html 표시
  * 	3. 페이지 번호 클릭시 콜백함수 호출
  * @param {number} totalItems 전체 아이탬 갯수
  * @param {number} currentPage 현재 페이지 번호
  * @param {Node} html를 표시할 엘리먼트
  * @param {Function} 페이지 번호 클릭시 호출할 콜백함수
  * @param {object} option {'pageSize', 'maxPages'}
  * @author gravity
  * @since	20200824	init
  */
 DS.pagination = function(totalItems, currentPage, $el, callbackFn, option){
	let opt = Pp.extend({'pageSize':10, 'maxPages':5}, option);
	
	//
	let pageSize = opt.pageSize ? opt.pageSize : 10;
	let maxPages = opt.maxPages ? opt.maxPages : 5;
	
 	let paging = Pp.paginate(totalItems, currentPage, pageSize, maxPages);

 	
 	let s = '';
	s += '<ul class="pagination">';
	
	//
	s += '	<li class="ico first" data-page-no="'+paging.startPage+'">처음</li>';
	
	//
	for(let i=0; i<paging.pages.length; i++){
		let pageNo = paging.pages[i];
		
		//
		if(currentPage == pageNo){		
			s += '	<li class="on"	data-page-no="'+pageNo+'">'+pageNo+'</li>';
		}else{
			s += '	<li class=""	data-page-no="'+pageNo+'">'+pageNo+'</li>';
		}
	}
	
	//
	if(0 === paging.pages.length){
		s += '	<li class=""	data-page-no="1">1</li>';
	}
	
	//
	s += '	<li class="ico end" data-page-no="'+paging.totalPages+'">마지막</li>';
	
	s += '</ul>';
 	
 	//화면에 표시
 	$el.html(s);
 	
 	//페이지 클릭 이벤트
 	$('ul.pagination > li').click(function(){
		$('ul.pagination > li').removeClass('on');
		//
 		let pageNo = $(this).data('page-no');
		//
		$('ul.pagination > li[data-page-no="'+pageNo+'"]').addClass('on');
		//
 		callbackFn(pageNo);
 	});
 };
 

window.addEventListener('load', function(){
	DS.init();
});


