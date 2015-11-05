function LoadImages(tagCon) {
	var publ = {},
	priv = {};

	publ.init = function() {
		priv.loadPrevMemeButton = $("#load_prev_meme_button");
		priv.loadNextMemeButton = $("#load_next_meme_button");
		priv.loadSpecificImageButton = $("#load_specific_meme_button");
		priv.saveMemeButton = $("#save_meme_button");
		priv.deleteMemeButton = $("#delete_meme_button");

		priv.loadPrevMemeButton.on("click", publ.loadPrevImage);
		priv.loadNextMemeButton.on("click", publ.loadNextImage);
		priv.loadSpecificImageButton.on("click", publ.loadSpecificImage);
		priv.saveMemeButton.on("click", publ.saveMeme);
		priv.deleteMemeButton.on("click", function() {
			var r = confirm("Delete meme?");
			if(r) {
				priv.deleteMeme();
			}
		});
	};


	publ.loadFirstImage = function() {
		$.ajax({
			url: '/inspect_db/load_meme',
			success: priv.onMemeLoaded,
			error: priv.onMemeLoaded
		});
	};

	priv.onMemeLoaded = function(e) {
		try {
			e = JSON.parse(e);
			$(".tag_checkbox").prop("checked", false);

			if(e.status && e.status=="no memes found") return;

			$("#img_here").attr("src", e.img_url);
			$("#title").html(e.title);
			$("#meme_id_input").val(e.id);

			if(e.tags) {
				priv.setActiveTags(e.tags);
			}
		} catch(exception) {
			$("#meme_id_input").val(parseInt($("#meme_id_input").val()) - 2);
			publ.loadNextImage();
		}
	};

	publ.loadNextImage = function() {
		$.ajax({
			url: '/inspect_db/load_meme',
			data: {currentId : $("#meme_id_input").val(), dir: "down"},
			success: priv.onMemeLoaded,
			error: priv.onMemeLoaded
		});
	};

	publ.loadPrevImage = function() {
		$.ajax({
			url: '/inspect_db/load_meme',
			data: {currentId : $("#meme_id_input").val(), dir: "up"},
			success: priv.onMemeLoaded,
			error: priv.onMemeLoaded
		});
	};

	publ.loadSpecificImage = function() {
		$.ajax({
			url: '/inspect_db/load_meme',
			data: {currentId : parseInt($("#meme_id_input").val()) + 1, dir: "down"},
			success: priv.onMemeLoaded,
			error: priv.onMemeLoaded
		});
	};

	priv.setActiveTags = function(tags) {
		tagCon.setActiveTags(tags);
	};

	publ.saveMeme = function() {
		var activeTags = tagCon.getActiveTags();
		var activeTagString = "";
		for(var i = 0; i < activeTags.length; i++) {
			activeTagString += activeTags[i];
			if(i != activeTags.length -1) {
				activeTagString += ",";
			}
		}

		$.ajax({
			url: '/inspect_db/add_tags_for_meme',
			data: {meme_id: $("#meme_id_input").val(), "tag_id" : activeTagString},
		});
	};

	priv.deleteMeme = function() {
		$.ajax({
			url: '/inspect_db/delete_meme',
			data: {meme_id: $("#meme_id_input").val()},
		});
		publ.loadNextImage();		
	};

	return publ;
}

var toType = function(obj) {
  return ({}).toString.call(obj).match(/\s([a-zA-Z]+)/)[1].toLowerCase()
}