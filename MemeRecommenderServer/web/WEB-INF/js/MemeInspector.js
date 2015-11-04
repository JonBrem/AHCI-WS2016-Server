function MemeInspector() {
	var publ = {},
	priv = {};

	priv.tags = [];
	priv.meme;

	publ.init = function() {
		priv.loadTags();
		$("#add_tag_button").on("click", priv.addTag);

		priv.meme = LoadImages(publ);
		priv.meme.init();
		priv.loadFirstImage();
	};

	// show tags
	priv.loadTags = function() {
		$.ajax({
			url: '/inspect_db/get_all_tags',
			success: function(e) {priv.onTagsLoaded(JSON.parse(e))},
			error: function(e) {priv.onTagsLoaded(JSON.parse(e.responseText))}
		});
	};

	priv.onTagsLoaded = function(tagsJson) {
		for(var i = 0; i < tagsJson.length; i++) {
			var t = Tag();
			priv.tags.push(t);

			t.create(tagsJson[i].name, tagsJson[i].id, $("#tags"));
		}
	};

	priv.addTag = function() {
		var t = Tag();
		priv.tags.push(t);

		t.create(false, false, $("#tags"));
	};

	
	priv.loadFirstImage = function() {
		priv.meme.loadFirstImage();		
	};

	publ.getActiveTags = function() {
		var activeTags = [];
		for(var i = 0; i < priv.tags.length; i++) {
			if(priv.tags[i].getCheckboxStatus()) {
				var tagId = priv.tags[i].getTagId();
				if(tagId == -1) {
					alert("Tag " + priv.tags[i].getTagName() + " does not exist on server yet!");
					return false;
				} else {
					activeTags.push(tagId);
				}
			}
		}
		return activeTags;
	};

	publ.setActiveTags = function(activeTagIds) {
		for(var i = 0; i < activeTagIds.length; i++) {
			for(var j = 0; j < priv.tags.length; j++) {
				if(activeTagIds[i] == priv.tags[j].getTagId()) {
					priv.tags[j].setCheckboxStatus(true);
				}
			}
		}
	};

	// select tags the image already has

	// add tags to image



	return publ;
}
