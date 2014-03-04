/**
@author: Anant Bhardwaj
@date: Feb 12, 2012

All front-end magic happen through this code.
*/

//check for cache update


window.applicationCache.addEventListener('updateready', function(){
        window.applicationCache.swapCache();
        window.location.reload();
}, false);


// try to first load the data from localStorage 

/* Global Data */


/* Private Data */
var login_id = localStorage.getItem('login_id')
var login_name = localStorage.getItem('login_name')
var starred = JSON.parse(localStorage.getItem('starred'))
var recommended = []

//var recommended = JSON.parse(localStorage.getItem('recommended'))



function refresh(_async_){
    if(!navigator.onLine){
        return
    }
    $.ajax({
        type: 'GET',
        async: _async_,
        url: '/data', 
        success: function(res) {            
            console.log("synced")               
            
            if(res.login_id!=null){
                login_id = res.login_id
                localStorage.setItem('login_id', login_id)
            }

            if(res.login_name != null){
                login_name = res.login_name
                localStorage.setItem('login_name', login_name)
            }            

            if(res.likes != null){
                starred = res.likes
                localStorage.setItem('starred', JSON.stringify(starred))
            }
                
            if(res.error){
                console.log('refresh/error')
            }
        }
    });
}


setInterval('refresh();', 60*1000)

refresh(false)
compute_recs()
/* data structure for pending stars */

function refresh_pending(){
    var star_pending = JSON.parse(localStorage.getItem('star_pending'))
    var unstar_pending = JSON.parse(localStorage.getItem('unstar_pending'))

    if(star_pending == null){
        star_pending = []
        localStorage.setItem('star_pending', JSON.stringify(star_pending))
    }

    if(unstar_pending == null){
        unstar_pending = []
        localStorage.setItem('unstar_pending', JSON.stringify(unstar_pending))
    }

    
}


refresh_pending()


String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

window.addEventListener("online", function() {
    enable_alert('You are online. Syncing new data with the server.')
    sync()
    refresh()
}, true);

 
window.addEventListener("offline", function() {
    enable_alert('You are offline. Any activities on this device would be synced when you come online.')
}, true);


function log(page){
    $.ajax({
        type:'GET',
        url:'/log/'+page, 
    });

}


function sync(){
    var star_pending = JSON.parse(localStorage.getItem('star_pending'))
    var unstar_pending = JSON.parse(localStorage.getItem('unstar_pending'))
   
    $.ajax({
        type:'POST',
        url:'/like/star',
        async: false, 
        data:{'papers': JSON.stringify(star_pending)}, 
        success: function(res) {

            console.log(res)
            //recommended = res.recs
            starred = res.likes
            /*
            if(recommended!=null)
                localStorage.setItem('recommended', JSON.stringify(recommended))
            */
            if(starred!= null)
                localStorage.setItem('starred', JSON.stringify(starred))
           
            star_pending = []    
            
            localStorage.setItem('star_pending', JSON.stringify(star_pending))
            
        }
    });

    $.ajax({
        type:'POST',
        url:'/like/unstar',
        async: false, 
        data:{'papers': JSON.stringify(unstar_pending)}, 
        success: function(res) {
            console.log(res)
            //recommended = res.recs
            starred = res.likes
            /*
            if(recommended!=null)
                localStorage.setItem('recommended', JSON.stringify(recommended))
            */
            if(starred!=null)
                localStorage.setItem('starred', JSON.stringify(starred))
            unstar_pending = []
            s_unstar_pending = []
            localStorage.setItem('unstar_pending', JSON.stringify(unstar_pending))    
            
        }
    });

    
}




function detect_mobile() { 
     if(navigator.userAgent.match(/Android/i)
         || navigator.userAgent.match(/webOS/i)
         || navigator.userAgent.match(/iPhone/i)
         || navigator.userAgent.match(/iPad/i)
         || navigator.userAgent.match(/iPod/i)
         || navigator.userAgent.match(/BlackBerry/i)
         || navigator.userAgent.match(/Windows Phone/i))
    {
        return true;
    } else {
        return false;
    }
}


jQuery.fn.highlight = function(pat) {
    function innerHighlight(node, pat) {
        var skip = 0;
        if (node.nodeType == 3) {
            var pos = node.data.toUpperCase().indexOf(pat);
            if (pos >= 0) {
                var spannode = document.createElement('span');
                spannode.className = 'text-highlight';
                var middlebit = node.splitText(pos);
                var endbit = middlebit.splitText(pat.length);
                var middleclone = middlebit.cloneNode(true);
                spannode.appendChild(middleclone);
                middlebit.parentNode.replaceChild(spannode, middlebit);
                skip = 1;
            }
        }
        else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
            for (var i = 0; i < node.childNodes.length; ++i) {
                i += innerHighlight(node.childNodes[i], pat);
            }
        }
        return skip;
        }
        return this.each(function() {
            innerHighlight(this, pat.toUpperCase());
        });
};



jQuery.fn.removeHighlight = function() {
    return this.find("span.text-highlight").each(function() {
        this.parentNode.firstChild.nodeName;
        with (this.parentNode) {
            replaceChild(this.firstChild, this);
            normalize();
        }
    }).end();
};



function get_params() {
    var vars = [], hash;
    var hashes = window.location.href.slice(
    window.location.href.indexOf('?') + 1).split('&');
    for (var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}


function get_hash() {
    if(window.location.href.indexOf('#') != -1)
        return window.location.href.slice(window.location.href.indexOf('#') + 1)
    else
        return window.location.href
}





Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};




function exists(recs, id){
    for(var r in recs){
        if(recs[r]['id'] == id){
            return true
        }
    }
    return false
}





function bind_events(){
    $("#headlink-right .mobile-nav").on('click',
        function(event){
          event.stopPropagation();
          $("#headlink-right a").toggleClass("toggle-nav");
        });
    $("#back-top a").click(function(){
      window.scrollTo(0,0);
    });

    $('.collapsible').off('click')
    $('.collapsible').on('click',
        function(event){
            event.stopPropagation();
            var target = $(this).attr("data")
            //console.log(target)
            $('#'+ target).toggle();
            if($('#'+target).is(':visible')){
                //$(this).find('.arrow').html('▾');
                $(this).find('.arrow').removeClass("arrow-right").addClass("arrow-down");
            }else{
                //$(this).find('.arrow').html('▸');
                $(this).find('.arrow').removeClass("arrow-down").addClass("arrow-right");
            }
        });

    $('.session-collapsible').off('click')
    $('.session-collapsible').on('click',
        function(event){
            enable_loading("opening a session...");
            event.stopPropagation();
            var s = $(this).parents('div.session:first')
            s.find('.paper-container').toggle();
            if(s.find('.paper-container').is(':visible')){
                s.find('.arrow').removeClass("arrow-right").addClass("arrow-down");
            }else{
                s.find('.arrow').removeClass("arrow-down").addClass("arrow-right");
            }
            disable_loading();
        });

    
    $("#refresh_recommendations").off('click')
    $("#refresh_recommendations").on('click', function(event){
        event.stopImmediatePropagation();

        refresh_recommendations()
    })

   
    
    if(detect_mobile()){
        $("body").addClass("touch-device");

        $('#search_papers').keyup(function(event){
            var str = $(this).val()
            if(str==""){
                reset_all_papers()
                $('#show_papers').show();
            }else{
                $('#show_papers').hide();
            }
            
        });


        $('#search_session').keyup(function(event){
            var str = $(this).val()
            if(str==""){
                reset_sessions()
                apply_filters()
            }
        });

        $('#search_sessions_btn').off('click')
        $('#search_sessions_btn').on('click', function(event){
                var str = $('#search_session').val()
                delay('simple_search_session("'+str+'");', 0);
            });


        $('#search_papers_btn').off('click')
        $('#search_papers_btn').on('click', function(event){
            var str = $('#search_papers').val()
            delay('simple_search_papers("'+str+'");', 0);
        });

        
    }else{
        $("body").addClass("no-touch-device");
        $('#search_session').keyup(function(event){
            var str = $(this).val()
            delay('search_session("'+str+'");', 300);
        });


        $('#search_papers').keyup(function(event){
            var str = $(this).val()
            if(str==""){
                $('#show_papers').show();
            }else{
                $('#show_papers').hide();
            }
            delay('search_papers("'+str+'");', 300);
        });
        

        $('#search_sessions_btn').off('click')
        $('#search_sessions_btn').on('click', function(event){
                var str = $('#search_session').val()
                delay('search_session("'+str+'");', 0);
            });


        $('#search_papers_btn').off('click')
        $('#search_papers_btn').on('click', function(event){
            var str = $('#search_papers').val()
            delay('search_papers("'+str+'");', 0);
        });
        

    }
    
   

    

    $('#show_likes').on('click', function(){
        if($("#likes tr:visible").length > 2){
                $("#likes tr:gt(1)").hide()
                $("#show_likes").html('Show All')
        }else{
                $("#likes tr").show()
                $("#show_likes").html('Show Less')
        }
        update_likes_count();         
    });
    

    $('#show_recs').on('click', function(){
        var n = $('#recs tr:visible').length
        if((n+5) < $("#recs tr").length){
            $('#recs tr:lt('+(n+5)+')').show()               
        }else{
            $("#recs tr").show()
            $("#show_recs").hide()
        }
        update_recs_count();         
    });
    

    $('#show_papers').on('click', function(){
        var n = $('#all_papers tr:visible').length
        if((n+25) < $("#all_papers tr").length){
            $('#all_papers tr:lt('+(n+25)+')').show()               
        }else{
            $("#all_papers tr").show()
            $("#show_papers").hide()
        }
        update_papers_count();         
    });

    $('#show_filters').on('click', function(){
      if ($(this).hasClass("expanded")){
        $(".more-filters").hide();
        $(this).text("Show More Filters");
        $(this).removeClass("expanded");
      } else {
        $(".more-filters").show();
        $(this).text("Show Less Filters");
        $(this).addClass("expanded");
      }

    });

    $('.send_tweet').on('click', function(event){
        event.stopPropagation();
        var id = $(this).parents("tr.paper").first().attr("data");
        var url = "http://mychi.csail.mit.edu/paper#" + id;
        //var title = $(this).siblings("a").text();
        var title = entities[id].title;
        var message = "Looking forward to seeing \"" + remove_special_chars(title) + "\""; 
        window.open ("https://twitter.com/share?" + 
            "url=" + encodeURIComponent(url) + 
            "&counturl=" + encodeURIComponent(url) +
            "&text=" + encodeURIComponent(message) + 
            "&hashtags=" + encodeURIComponent('chi2013') + 
            "&via=" + encodeURIComponent('mychi2013'), 
            "twitter", "width=500,height=300");
    });

    $('.send_session_tweet').on('click', function(event){
        event.stopPropagation();
        var id = $(this).parents(".session").first().attr("data");
        var url = document.location;
        //var title = $(this).siblings(".session-title").text();
        var title = sessions[id].s_title;
        var message = "Looking forward to seeing \"" + remove_special_chars(title) + "\" " + sessions[id].date + " | " + sessions[id].time + " | " + sessions[id].room;
        window.open ("https://twitter.com/share?" + 
            //"url=" + encodeURIComponent(url) + 
            //"&counturl=" + encodeURIComponent(url) +
            "&text=" + encodeURIComponent(message) + 
            "&hashtags=" + encodeURIComponent('chi2013') + 
            "&via=" + encodeURIComponent('mychi2013'), 
            "twitter", "width=500,height=300");
    });

    $('.send_email').on('click', function(event){
        event.stopPropagation();
        var id = $(this).parents("tr.paper").first().attr("data");
        var url = "http://mychi.csail.mit.edu/paper#" + id;
        var title = remove_special_chars(entities[id].title);
        var message = "Hi there!\n\nI found this interesting paper at CHI 2013 that you may be interested in:\n"
          + title + "\n" + url;
        var link = "mailto:";
        //var link = "<a id='email-link' href='mailto:";
        link += "?to=&subject=" + encodeURIComponent("Paper at CHI2013: " + title);
        link += "&body=" + encodeURIComponent(message);
        //var link = $(link); 
        //link.appendTo("#page");
        //$("#email-link").trigger("click");//.remove();
        window.location.href = link;
    });


    $('.send_session_email').on('click', function(event){
        event.stopPropagation();
        var id = $(this).parents(".session").first().attr("data");
        var url = document.location;
        var title = remove_special_chars(sessions[id].s_title);
        var message = "Hi there!\n\nI found this interesting session at CHI 2013 that you may be interested in:\n"
          + title + "\n" + url + "\n" + sessions[id].date + " | " + sessions[id].time + " | " + sessions[id].room;
        var link = "mailto:";
        //var link = "<a id='email-link' href='mailto:";
        link += "?to=&subject=" + encodeURIComponent("Session at CHI2013: " + title);
        link += "&body=" + encodeURIComponent(message);
        //var link = $(link); 
        //link.appendTo("#page");
        //$("#email-link").trigger("click");//.remove();
        window.location.href = link;
    });
}



var delay = (function(){
  var timer = 0;
  return function(callback, ms){
    clearTimeout (timer);
    timer = setTimeout(callback, ms);
  };
})();

function remove_special_chars(str){
    if (str == null || str == "null")
      return "";
    var result = str;
    result = result.replace(/¬/g, "-"); 
    result = result.replace(/×/g, "x"); 
    result = result.replace(/–/g, "-"); 
    result = result.replace(/‘/g, "'"); 
    result = result.replace(/’/g, "'"); 
    result = result.replace(/“/g, "\""); 
    result = result.replace(/”/g, "\""); 
    result = result.replace(/\\"/g, "\""); 
    result = result.replace(/â€”/g, "-");
    result = result.replace(/â€"/g, "-");
    result = result.replace(/â€˜/g, "'");
    result = result.replace(/â€œ/g, "\"");
    result = result.replace(/â€/g, "\"");
    result = result.replace(/Ã©/g, "é");
    result = result.replace(/\\u2013/g, "-");
    result = result.replace(/\\u00ac/g, "-");
    result = result.replace(/\\u2014/g, "-");
    result = result.replace(/\\u2018/g, "'");
    result = result.replace(/\\u2019/g, "'");
    result = result.replace(/\\u2022/g, "*");
    result = result.replace(/\\u201c/g, "\"");
    result = result.replace(/\\u201d/g, "\"");
    result = result.replace(/â€™/g, "'");
    result = result.replace(/â€“/g, "-");
    result = result.replace(/™/g, "(TM)"); 
    result = result.replace(/\\\//g, "/"); 
    result = result.replace(/\\/g, ""); 
    result = result.replace(/\\ \\/g, ""); 
    return result;
}


function search_session(str){
    //$('.filter').removeClass('active')
    //$('.f_all').addClass('active')
    reset_sessions()
    apply_filters()
    if(str=="" || str == $('#search_session').attr("title")){        
        return
    }
    var regex_str = ''
    var words = str.split(' ')
    for (var i=0;i<words.length; i++){
        regex_str += '(?=.*\\b'+words[i]+'.*\\b)'
    }
    var s =  new RegExp(regex_str , 'i')
    $('.session:visible').each(function(){
        $(this).parent().prev().hide()
    });

      
    $('.session:visible').each(function(){
        if(s.test($(this).text())){
            $(this).show()   
            var p = $(this).attr("data")
            $("#"+p).show()
            $(this).find('.arrow').removeClass("arrow-right").addClass("arrow-down");
            $(this).highlight(str);
            //$(this).text().indexOf(str)        
        }else{
            $(this).hide()
            var p = $(this).attr("data")
            $("#"+p).hide()
            $(this).find('.arrow').removeClass("arrow-down").addClass("arrow-right");

        }

    });

    $('.session:visible').each(function(){
        $(this).parent().prev().show()
        
    });
    
    update_sessions_count(); 
    
}


function simple_search_session(str){

    reset_sessions()
    apply_filters()
    if(str=="" || str == $('#search_session').attr("title")){        
        return
    }
    
    $('.session:visible').each(function(){
        $(this).parent().prev().hide()
    });

       
    $('.session').each(function(){
        if($(this).text().toLowerCase().indexOf(str.toLowerCase())!=-1){
            $(this).show()            
        }else{
            $(this).hide()
        }

    });

    $('.session:visible').each(function(){
        $(this).parent().prev().show()
        $('.session .paper').show()
    });
    update_sessions_count(); 
    
}

function search_papers(str){
    if(str==""){
        reset_all_papers()
        return
    }
    var regex_str = ''
    var words = str.split(' ')
    for (var i=0;i<words.length; i++){
        regex_str += '(?=.*\\b'+words[i]+'.*\\b)'
    }
    var s =  new RegExp(regex_str , 'i')
    
    //console.log(s)
       
    $('#all_papers .paper').each(function(){
        if(s.test($(this).text())){
            $(this).show()
            
        }else{
            $(this).hide()
        }

    });
    
    
    update_papers_visible_count(); 
}


function simple_search_papers(str){
   
       
    $('#all_papers .paper').each(function(){
        if($(this).text().toLowerCase().indexOf(str.toLowerCase())!= -1){
            $(this).show()
            
        }else{
            $(this).hide()
        }

    });
    

   

    update_papers_visible_count(); 
}




function refresh_recommendations(){
    compute_recs()
    populate_recs()
    update_recs()
}


function get_short_session_info_of_paper(id){
  var result = "";
  var session = sessions[entities[id].session];
  var date = "";
  if (session.date == "Monday")
    date = "Mon";
  if (typeof session !== "undefined"){
    result += session.date.substring(0,3) + " " + session.time.substr(0,5) + " | " + session.room + " | " + session.s_title;
  }
  return result;
}

function get_session_info_of_paper(id){
  var result = "";
  var session = sessions[entities[id].session];
  if (typeof session !== "undefined"){
    result += session.date + " | " + session.time + " | " + session.room + " | " + session.s_title;
  }
  return result;
}



function select_paper(id){
    if(window.location.pathname.endsWith('/paper')){
        window.location.hash = "#" + id;
        window.location.reload(false);
    }else{
        window.location.href = 'paper#'+id
    }
    window.scrollTo(0,0)
}

function isMyPaper(id){
  if ($.inArray(id, own_papers) > -1)
    return true;
  return false;
}


function get_paper_html(id){
    if(entities[id] == null)
        return ''
    var raw_html = '<tr data= "' + id + '" class="clickable paper ' + id
    if(exists(recommended, id)){
        raw_html += ' recommended'
    }
    if(starred.indexOf(id) >= 0){
        raw_html += ' highlight'
    }
    if(entities[id].hm){
        raw_html += ' p_hm'
    }
    if(entities[id].award){
        raw_html += ' p_award'
    }
    raw_html += '">'
      
    raw_html += '<td class="metadata">'   
    if(starred.indexOf(id) == -1){
        raw_html += '<div class="star star-open p_star" data="'+ id + '" onclick="handle_star(event);">'        
    }else{
        raw_html += '<div class="star star-filled p_star" data="'+ id + '" onclick="handle_star(event);">'       
    }
    raw_html += '</div>'
    
    raw_html += '</td>'
    
    raw_html += '<td class="content">'    
    raw_html += '<ul>'

    raw_html += '<li class="paper-title"><h3><span class="link" onclick=select_paper("'+id+'")>'+ remove_special_chars(entities[id].title) +'</span>'
    if(entities[id].type!=null){
        raw_html += '<span class="paper-subtype">' + ' ' + entities[id].type + '</span>'
    }
    raw_html += '</h3>'
    raw_html += '</li>'


    raw_html += '<li class="paper-authors">'
    for(author in entities[id].authors){
        if(entities[id].authors[author] != null){
            raw_html += ' ' + remove_special_chars(entities[id].authors[author].name) + '&nbsp;&nbsp;&nbsp;&nbsp;'
        }
    }
    raw_html += '</li>'
   
    raw_html += '<span class="rec-icon">recommended</span>'
    

    if (entities[id].abstract != null)
        if(entities[id].abstract != ""){
            raw_html += '<li class="paper-cb">'+ remove_special_chars(entities[id].abstract.slice(0,350)) + '...</li>'
        }else{
            raw_html += '<li class="paper-cb">ABSTRACT not available.</li>'
        }
    
    if(entities[id].keywords != null){
        raw_html += '<li class="paper-keywords">' + entities[id].keywords + '</li>'
    }
    raw_html += '</ul>'
    raw_html += '</td>'
    
    raw_html += '</tr>'

    return raw_html
}





function get_session_html(id, day, time, slot_name, room){
    if(sessions[id]== null){
        return ''
    }
    var award=''
    if(sessions[id].award){
        award += ' s_award'
    }
    if(sessions[id].award || sessions[id].hm){
        award += ' s_hm'
    }
    var raw_html = '<div class="session ' + id  + ' ' + day + ' ' + slot_name + ' '
              + ' ' + room + '" data="' + id + '">'
    raw_html += '<table class="session-container session-collapsible" data="' + id + '"><tr class="clickable">'
    
    raw_html += '<td class="metadata">'     
    raw_html += '<div class="star star-open s_star" data="'+ id + '" onclick="handle_session_star(event);">'
    raw_html += '</div>'        
    raw_html += '</td>'
    
    raw_html += '<td class="content">'  
    raw_html += '<ul>'
    raw_html += '<li><h3><span class="arrow arrow-right"></span> <span class="session-title">'+ sessions[id].s_title + '</span>'
   /*
    raw_html += '<span class="send_session_tweet"></span>'
    raw_html += '<span class="send_session_email"></span>'
    */
    raw_html += '</h3></li>'
    raw_html += '<li class="session-icons"><span class="award-icon"></span><span class="hm-icon"></span><span class="rec-icon">recommended</span>'

    
    
    raw_html += '</li>';
    raw_html += '<li class="session-info"><span class="session-room">Room: ' + room + '</span></li>'
    raw_html += '</ul>'

    
    raw_html += '<div class="timeline">'
    var size = sessions[id].submissions.length
    var weight = []
    var sum = 0
    for(i=0; i<size; i++){
        /*
        if(entities[sessions[id].submissions[i]].subtype == 'Note'){
            weight[i] = 0.5
        }else{
            weight[i] = 1.0
        }
        */
        weight[i] = 1.0
        sum += weight[i]
    }
    
    for(var i=0; i< size; i++){
        var w = 100*(weight[i]/sum)
        raw_html += '<div style="width:' + w + '%;"></div>'
    }
    raw_html += '</div>'
    raw_html += '</td>'
    
    raw_html += '</tr>'
    raw_html += '</table>'
    raw_html += '<table id="' +id +'" class="paper-container" style="display:none; padding-left:20px;">'
    for(var i in sessions[id].submissions){        
        raw_html += get_paper_html(sessions[id].submissions[i]);        
    }
    raw_html += '</table>'
    raw_html += '</div>'
    return raw_html
}



function get_selected_paper_html(id){
    if(entities[id] == null)
      return null
    raw_html += '<tbody>'
    var raw_html = '<tr data= "' + id + '" class="paper ' + id
    if(exists(recommended, id)){
        raw_html += ' recommended'
    }
    if(starred.indexOf(id) >= 0){
        raw_html += ' highlight'
    }
    if(entities[id].hm){
        raw_html += ' p_hm'
    }
    if(entities[id].award){
        raw_html += ' p_award'
    }
    raw_html += '">'


    raw_html += '<td class="metadata">'   
    if(starred.indexOf(id) == -1){
        raw_html += '<div class="star star-open p_star" data="'+ id + '" onclick="handle_star(event);">'        
    }else{
        raw_html += '<div class="star star-filled p_star" data="'+ id + '" onclick="handle_star(event);">'       
    }
    raw_html += '</div>'
    
    raw_html += '</td>'
    
    raw_html += '<td class="content">'    
    raw_html += '<ul>'



    raw_html += '<h3>' + entities[id].title
    if(entities[id].subtype != null){
        raw_html += '<span class="paper-subtype">' + entities[id].subtype + '</span>'
    }
    
    
    raw_html += '</h3>';

    raw_html += '<li class="paper-authors">'
    for(var author in entities[id].authors){
        
       raw_html += '<span class="author"><span class="author-name">' 
                + entities[id].authors[author].name 
                + '</span>';
        if(entities[id].authors[author].affiliation != null && entities[id].authors[author].location !=null){
        raw_html += '<span class="author-affiliation">'
                + entities[id].authors[author].affiliation + ', ' + entities[id].authors[author].location 
                + '</span>';
        }
        
    }
    raw_html += '</li>'
    raw_html += '<li class="paper-selected-session">' + get_session_info_of_paper(id) + '</li>'
    raw_html += '<hr />'
    raw_html += '<li>' + entities[id].abstract + '</li>'
    if(entities[id].keywords != null){
        raw_html += '<li class="paper-keywords">' + entities[id].keywords + '</li>'
    }
    raw_html += '</ul>'
    raw_html += '</td>'
    raw_html += '</tr>'
    raw_html += '</tbody>'
    raw_html += '</table>'
    return raw_html
}







function update_session_view(){
    $( ".session" ).each(function(s_index) {
        var session = $(this)
        $(this).find('.paper-container').find('.star').each(function(p_index){
            if($(this).hasClass('star-filled')){
                session.find('.timeline').children("div").eq(p_index).addClass("filled_yellow");
            }else{
                session.find('.timeline').children("div").eq(p_index).removeClass("filled_yellow");                  
            }
                        

        });

        $(this).find('.paper-container tr').each(function(p_index){
            if($(this).hasClass('recommended')){
                session.addClass('s_recommended')
                session.find('.timeline').children("div").eq(p_index).addClass("filled_blue");
            }else{                
                session.find('.timeline').children("div").eq(p_index).removeClass("filled_blue");
            }               

        });

        
        
        if($(this).find('.paper-container').find('.recommended').length > 0){
            $(this).find('.session-container').find('tr').addClass('recommended')
            session.addClass('s_recommended')
        }else{
            $(this).find('.session-container').find('tr').removeClass('recommended')
            session.removeClass('s_recommended')
        }
       
        if($(this).find('.paper-container').find('.star-filled').length > 0){
              $(this).find('.session-container').find('.star').removeClass('star-open').addClass('star-filled')
              $(this).find('.session-container').find('tr').addClass('highlight')
              session.addClass('s_starred')
        }else{
            $(this).find('.session-container').find('.star').removeClass('star-filled').addClass('star-open')
            $(this).find('.session-container').find('tr').removeClass('highlight')
            session.removeClass('s_starred')
        }
        
    });   

    update_sessions_count();
}


function update_recs(){
      $('.paper').removeClass('recommended')
      for(var r in recommended){
            $('.'+recommended[r]['id']).each(function(){
                $(this).addClass('recommended')
            });
      }

}


function add_pending_unstar(paper_id){
    var star_pending = JSON.parse(localStorage.getItem('star_pending'))
    var unstar_pending = JSON.parse(localStorage.getItem('unstar_pending'))
    var i =  star_pending.indexOf(paper_id)
    if( i == -1){
        unstar_pending.push(paper_id)
    }else{
        star_pending.splice(i, 1)
    }
    localStorage.setItem('star_pending', JSON.stringify(star_pending))
    localStorage.setItem('unstar_pending', JSON.stringify(unstar_pending))
}



function add_pending_star(paper_id){
    var star_pending = JSON.parse(localStorage.getItem('star_pending'))
    var unstar_pending = JSON.parse(localStorage.getItem('unstar_pending'))
    var i =  unstar_pending.indexOf(paper_id)
    if( i == -1){
        star_pending.push(paper_id)
    }else{
        unstar_pending.splice(i, 1)
    }
    localStorage.setItem('star_pending', JSON.stringify(star_pending))
    localStorage.setItem('unstar_pending', JSON.stringify(unstar_pending))

}


function handle_session_star(event){
    enable_alert("updating information..."); 
    event.stopPropagation();
    var obj = $(event.target).parents("td:first").find('.s_star')
    var session_id = obj.attr("data")
    var papers = sessions[session_id]['submissions']
    //console.log(papers)
    if(obj.hasClass('star-filled')){
        $('.'+obj.attr('data')).each(function(){
            $(this).find('.p_star').removeClass('star-filled').addClass('star-open')
            $(this).find('.paper').removeClass('highlight')
        })
        if(!navigator.onLine){
            enable_alert("You unliked a session. You are not online -- updating information locally.");
            for(var paper_id in papers){
                var i =  starred.indexOf(papers[paper_id])
                starred.splice(i, 1)
                add_pending_unstar(papers[paper_id])
            }            
            localStorage.setItem('starred', JSON.stringify(starred))
            update_session_view()
            apply_filters()
        }else{
            $.post('/like/unstar', {'papers': JSON.stringify(papers), 'session': JSON.stringify([session_id])}, function(res) {
                for(var paper_id in papers){
                    var i =  starred.indexOf(papers[paper_id])
                    starred.splice(i, 1)                    
                }
                $('.'+obj.attr('data')).each(function(){
                    $(this).find('.p_star').removeClass('star-filled').addClass('star-open')
                    $(this).find('.paper').removeClass('highlight')
                })
                starred = res.likes
                compute_recs()
                localStorage.setItem('starred', JSON.stringify(starred))
                //localStorage.setItem('recommended', JSON.stringify(recommended))
                update_recs()
                update_session_view()
                apply_filters()
            })
            .done(function(){
                enable_alert("You unliked a session.");
            });
        }
       
    }else{
        $('.'+obj.attr('data')).each(function(){
            $(this).find('.p_star').removeClass('star-open').addClass('star-filled')
            $(this).find('.paper').addClass('highlight')
            update_session_view()
            apply_filters()
        })
        if(!navigator.onLine){
            enable_alert("You liked a session. You are not online -- updating information locally.");
            for(var paper_id in papers){
                starred.push(papers[paper_id])
                add_pending_star(papers[paper_id])
            }
            localStorage.setItem('starred', JSON.stringify(starred))
        }else{
            $.post('/like/star', {'papers': JSON.stringify(papers), 'session': JSON.stringify([session_id])}, function(res) {
                for(var paper_id in papers){
                    starred.push(papers[paper_id])
                }
                $('.'+obj.attr('data')).each(function(){
                    $(this).find('.p_star').removeClass('star-open').addClass('star-filled')
                    $(this).find('.paper').addClass('highlight')
                })
                starred = res.likes
                compute_recs()
                localStorage.setItem('starred', JSON.stringify(starred))
                //localStorage.setItem('recommended', JSON.stringify(recommended))
                update_recs()
                update_session_view()
                apply_filters()
            })
            .done(function(){
                enable_alert("You liked a session.");
            });
        }
        
    }
    


}



function compute_recs(){
    var t_recs = []
    var final_recs = []
    if(typeof starred == "undefined" || starred == null ){
        return null
    }
    if(typeof offline_recs == "undefined" || offline_recs == null ){
        return null
    }
    starred.forEach(function (p){
        var p_recs = offline_recs[p]
        if (p_recs != null) {        
            p_recs.forEach(function(p_rec){
                var p_id = Object.keys(p_rec)[0]
                if(! exists(t_recs, p_id) && starred.indexOf(p_id) == -1){
                    t_recs.push({'id':p_id, 'score':p_rec[p_id]})
                }
            })
        }
    })
    var score = 0.3
    var len = 16
    if(t_recs.length < 16){
        len = t_recs.length
    }
    //console.log(t_recs)
    while(final_recs.length < len && score > 0.02){
        for(var i=0; i<t_recs.length; i++){
            if(t_recs[i]['score'] > score){
                if(!exists(final_recs, t_recs[i]['id'])){
                    final_recs.push({'id':t_recs[i]['id'], 'score':t_recs[i]['score']})
                }
            }
        }
        score = score - 0.01

    }
    console.log(final_recs)
    recommended = final_recs
}





function handle_star(event){ 
    //$("#refresh_recommendations").show();
    enable_alert("updating information..."); 
    var obj = $(event.target).parents("td:first").find('.p_star')
    var paper_id = obj.attr("data")
    //console.log($(window).scrollTop(), $(event.target).parents("td:first").position().top);
    var position_old = $(event.target).parents("td:first").position().top;
    var position_delta = 0;
    //$(event.target).parents("tr").effect("highlight", {}, 5000);
    if(obj.hasClass('star-filled')){
        $('.'+obj.attr('data')).each(function(){
            $(this).find('.p_star').removeClass('star-filled').addClass('star-open')
            $(this).removeClass('highlight')
        })
        if(!navigator.onLine){
            enable_alert("You unliked a paper. You are not online -- updating information locally.");
            var i =  starred.indexOf(paper_id)
            starred.splice(i, 1)
            add_pending_unstar(paper_id)
            populate_likes(starred)
            localStorage.setItem('starred', JSON.stringify(starred))
        }else{
            $.post('/like/unstar', {'papers': JSON.stringify([paper_id])}, function(res) {
              if(!res.error){
                $('.'+obj.attr('data')).each(function(){
                    $(this).find('.p_star').removeClass('star-filled').addClass('star-open')
                    $(this).removeClass('highlight')
                })

                var i =  starred.indexOf(paper_id)
                starred.splice(i, 1)
                populate_likes(starred)
                compute_recs()
                localStorage.setItem('starred', JSON.stringify(starred))
                //localStorage.setItem('recommended', JSON.stringify(recommended))
                
                if($("#recs tr").length == 0){
                    populate_recs()
                }else{
                    append_recs()
                }
                update_recs()
                update_session_view()
              }
            })
            .done(function(){
                //console.log($(window).scrollTop(), $(event.target).parents("td:first").position().top);
                enable_alert("You unliked a paper.");
                var scroll_current = $(window).scrollTop();
                position_delta = $(event.target).parents("td:first").position().top - position_old;
                if (position_delta != 0)
                  $('html, body').scrollTop(scroll_current + position_delta);
            });
        }
    }else{
        $('.'+obj.attr('data')).each(function(){
            $(this).find('.p_star').removeClass('star-open').addClass('star-filled')
            $(this).addClass('highlight')
        })
        if(!navigator.onLine){
            enable_alert("You liked a paper. You are not online -- updating information locally.");
            starred.push(paper_id)
            add_pending_star(paper_id)
            populate_likes(starred)
            localStorage.setItem('starred', JSON.stringify(starred))
        }else{
            $.post('/like/star', {'papers': JSON.stringify([paper_id])}, function(res) {
              if(!res.error){
                $('.'+obj.attr('data')).each(function(){
                    $(this).find('.p_star').removeClass('star-open').addClass('star-filled')
                    $(this).addClass('highlight')
                })
                starred.push(paper_id)
                populate_likes(starred)
                compute_recs()
                localStorage.setItem('starred', JSON.stringify(starred))
                //localStorage.setItem('recommended', JSON.stringify(recommended))
                
                if($("#recs tr").length == 0){
                    populate_recs()
                }else{            
                    append_recs()
                }
                update_recs()
                update_session_view()
                
              }
            })

            .done(function(){
                //console.log($(window).scrollTop(), $(event.target).parents("td:first").position().top);
                enable_alert("You liked a paper.");
                var scroll_current = $(window).scrollTop();
                position_delta = $(event.target).parents("td:first").position().top - position_old;
                if (position_delta != 0)
                  $('html, body').scrollTop(scroll_current + position_delta);
            });
        }



    }
}



function load_paper(){
    var paper_id = get_hash()
    console.log(paper_id)
    var selected_paper_html = get_selected_paper_html(paper_id)
    $('#selected_paper').find('.form').html(selected_paper_html)
    $('#similar_papers').html('')
    var recs = offline_recs[paper_id]
    var raw_html = ''
    for(var i = 0; i< recs.length; i++){        
        raw_html += get_paper_html(Object.keys(recs[i])[0])            
    } 
    $('#similar_papers').html(raw_html)     
} 



function update_papers_count(){
    setTimeout('update_papers_count_async();', 0)
}

function update_papers_count_async(){
    $("#papers_toggle .count").text("(" + $("#all_papers tr").length + ")");  
}

function update_papers_visible_count(){
    setTimeout('update_papers_visible_count_async();', 0)
}

function update_papers_visible_count_async(){
    $("#papers_toggle .count").text("(" + $("#all_papers tr:visible").length + ")");  
}

function update_recs_count(){
    setTimeout('update_recs_count_async();', 0)
}

function update_recs_count_async(){
    $("#recs_toggle .count").text("(" + $("#recs tr:visible").length + ")");  
}

function update_likes_count(){
    setTimeout('update_likes_count_async();', 0)
}

function update_likes_count_async(){
    $("#likes_toggle .count").text("(" + $("#likes tr").length + ")");  
}

function update_sessions_count(){
    setTimeout('update_sessions_count_async();', 0)
}

function update_sessions_count_async(){
    if ($("#program .session").length == $("#program .session:visible").length)
      $("#search-results .count").text("all");
    else
      $("#search-results .count").text($("#program .session:visible").length);  
}



function reset_all_papers(){
    $("#all_papers tr").show()
    $("#all_papers tr:gt(24)").hide()  

    if($("#all_papers tr:visible").length == $("#all_papers tr").length){
        $('#show_papers').hide();
    }else{
        $('#show_papers').show();
    }         
    update_papers_count();
}


function reset_sessions(){
    $('.session').show()
    $('.session').removeHighlight()
    $('.session-timeslot').each(function(){
        $(this).prev().hide()
    });  

    $('.session').each(function(){
        $(this).parent().prev().show()
        var p = $(this).attr("data")
        $("#"+p).hide()
        $(this).find('.arrow').removeClass("arrow-down").addClass("arrow-right");
        
    });
    update_sessions_count(); 
}

function append_recs(){  
    var visible_recs = []
    $("#recs tr:visible").each(function(){
        var d = $(this).attr("data")
        visible_recs.push(d)
    })
    var n = $("#recs tr:visible").length
    $("#recs tr:hidden").remove()  
    var raw_html = ''
    for(var r in recommended){
        if(visible_recs.indexOf(recommended[r].id) == -1){
            raw_html += get_paper_html(recommended[r].id)
        }
    }
    $("#recs").append($(raw_html))
    $("#recs tr:gt("+(n-1)+")").hide() 
    if($("#recs tr:visible").length == $("#recs tr").length){
        $('#show_recs').hide();
    }else{
        $('#show_recs').show();
    }   

    
}



function populate_papers(){
    if(typeof entities == "undefined" || entities == null){
        console.log("Error populating papers list.")
        return
    }
    var raw_html = ''       
    for(var e in entities){
        raw_html += get_paper_html(e)
    }
    $("#all_papers").html(raw_html)
    $("#all_papers tr:gt(24)").hide()  

    if($("#all_papers tr:visible").length == $("#all_papers tr").length){
        $('#show_papers').hide();
    }else{
        $('#show_papers').show();
    }         
    update_papers_count();
}


function populate_recs(){  
    if(typeof recommended == "undefined" || recommended == null){
        console.log("Error populating recommendations.")
        return
    }
    if(typeof entities == "undefined" || entities == null){
        console.log("Error fetching entities.")
        return
    }
    var raw_html = ''   
    for(var r in recommended){
        raw_html += get_paper_html(recommended[r]['id'])
    }
    $("#recs").html(raw_html)

    $("#recs tr:gt(4)").hide()  

    if($("#recs tr:visible").length == $("#recs tr").length){
        $('#show_recs').hide();
    }else{
        $('#show_recs').show();
    }         
      
    update_recs_count(); 
}


function populate_likes(){
    if(typeof starred == "undefined" || starred == null){
        console.log("Error populating stars.")
        return
    }
    if(typeof entities == "undefined" || entities == null){
        console.log("Error fetching entities.")
        return
    }
    var raw_html = '' 
    for(var i = starred.length; i>=0 ; i--){
       raw_html += get_paper_html(starred[i])
    }
    $("#likes").html(raw_html)
    if($("#likes tr").length <= 2){
        $('#show_likes').hide();
    }else{
         $('#show_likes').show()
        if($('#show_likes').html() == 'Show All'){
            $("#likes tr:gt(1)").hide()           
        }
    }  
    update_likes_count();
}


function populate_schedule(){
    if(typeof schedule == "undefined" || schedule == null){
        console.log("Error populating schedule.")
        return
    }
    if(typeof sessions == "undefined" || sessions == null){
        console.log("Error fetching sessions.")
        return
    }
    for(var day in schedule){
        var raw_html = '<div id = "'+schedule[day].day+'"></div>'
        for(slot in schedule[day].slots){
            raw_html += '<h3 class="collapsible-title collapsible" \
                    data="'+schedule[day].slots[slot].slot_id+'"> \
                    <span class="arrow arrow-down"></span>'+ schedule[day].day + ', ' + 
                    schedule[day].slots[slot].time + '</h3>'
            raw_html += '<div id = "'+schedule[day].slots[slot].slot_id+'" class="session-timeslot">'
            for(session in schedule[day].slots[slot].sessions){
                raw_html += get_session_html(schedule[day].slots[slot].sessions[session].session, 
                        schedule[day].day, schedule[day].slots[slot].time, schedule[day].slots[slot].slot_class, 
                        schedule[day].slots[slot].sessions[session].room)
            }
            raw_html += '</div>'
        }
        $("#program").append(raw_html)        
    }
    update_recs()
    update_session_view()
}

function populate_filters(){
    if(typeof s_filters != "object" || s_filters == null){
        return
    }
    var filter_html = ''
    for(var f in s_filters){
        var filter = s_filters[f]
        filter_html += '<tr>'
        filter_html += '<td style="vertical-align:top">'
        filter_html += '<h4 style="display:inline-block">' + filter.label + '</h4>'
        filter_html += '</td>'
        filter_html += '<td>'
        filter_html += '<div style="display:inline-block">'
        for(var i in filter.vals){
            val = filter.vals[i]
            filter_html += '<a href="#" data="' + val.data + '" class="' + val.class + '" type="' + val.type + '">' + val.label + '</a> '
        }
        filter_html += '</div>'
        filter_html += '</td>'
        filter_html += '</tr>'
    }
    $("#filter_schedule").html(filter_html)
}



function apply_filters(){
    var day_classes = '.'+$('.day.active').attr("data")
    var time_classes = '.'+$('.time.active').attr("data")
    var papers_classes = '.'+$('.p_session.active').attr("data")
    

    var select_class = $('.session')
    if(day_classes != '.all'){
        select_class = select_class.filter(day_classes)
    }
    
    if(time_classes!='.all'){               
        select_class = select_class.filter(time_classes)                
    }

    if(papers_classes!='.all'){             
        select_class = select_class.filter(papers_classes)              
    }
   $('.session').hide();
   $('.session-timeslot').each(function(){
        $(this).prev().hide()
    });

   select_class.show();
   select_class.each(function(){
        $(this).parent().prev().show()
    });

   update_sessions_count(); 
}



function setup_filters(){
    $('.filter').off('click')
    $('.filter').on('click', function(){       
        var attr = $(this).attr("type")
        $('.'+attr).removeClass('active')

        $(this).addClass('active')
        $('#search_sessions_btn').click()
        
    });
}



function enable_loading(msg){
  $("body .modal .message").text(msg);
  $("body").addClass("loading");
}



function disable_loading(){
  $("body").removeClass("loading");
}



function enable_alert(msg){
  $("body .alert .message").text(msg);
  $("body").addClass("notice");
  setTimeout(function(){
    $("body").removeClass("notice");
  }, 3000);
}
