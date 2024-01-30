<FORM METHOD=GET ACTION=/search id="searchform">
<input class="search__input" type="text" name="box" placeholder=" 输入搜索关键词"  >
<input type="hidden" name="url">
<input type="hidden" name="type" value="video">
</FORM>
<script>
<style type="text/css">
    .markdown-body{
        display: none !important;
    }
</style>
function addsearch() {
    let searchObj = document.querySelector(".markdown-body form");
    if(searchObj){
      let headRight = document.querySelector(".header-right");
      headRight.prepend(searchObj);
      searchObj.querySelector('input').style.width="90%";
    }
    else{
      setTimeout(()=>{
        addsearch();
      },333);
    }
  }
  addsearch();
</script>