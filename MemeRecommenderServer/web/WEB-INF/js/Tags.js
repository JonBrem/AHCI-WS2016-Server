function Tag() {
	var publ = {},
	priv = {};

	priv.tagId = -1;

	priv.rootElement;
	priv.saveElement;
	priv.inputElement;

	publ.create = function(name, id, appendTo) {
		priv.rootElement = $('<div class="col-md-4"><div class="tag_root input-group">' +
				'<input type="text" class="tag_input form-control" value="' + (name === false? '' : name) + '">' +
				'<span class="input-group-addon">' +
	        		'<input type="checkbox" aria-label="" class="tag_checkbox">' + 
	      		'</span>' +
	      		'<span class="input-group-btn">' + 
	        		'<button class="btn btn-default save_button" type="button">Save</button>' + 
	      		'</span>' + 
			'</div></div>');

		if(id !== false) {
			priv.tagId = id;
		}

		appendTo.append(priv.rootElement);
		priv.inputElement = priv.rootElement.find('.tag_input');
		priv.saveElement = priv.rootElement.find(".save_button");

		priv.saveElement.on("click", priv.save);
	};

	priv.save = function() {

		if(priv.tagId == -1) {
			$.ajax({
				url: '/inspect_db/add_new_tag',
				data: {tag_name: priv.inputElement.val()},
				success: priv.onSaveCallback,
				error: priv.onSaveCallback
			});
		} else {
			$.ajax({
				url: '/inspect_db/add_new_tag',
				data: {tag_name: priv.inputElement.val(), tag_id: priv.tagId},
				success: priv.onSaveCallback,
				error: priv.onSaveCallback
			});
		}
	};


	priv.onSaveCallback = function(e) {
		e = JSON.parse(e);
		priv.tagId = e.tagId;
	};

	publ.getTagId = function() {
		return priv.tagId;
	};

	publ.setCheckboxStatus = function(status) {
		priv.rootElement.find('.tag_checkbox').prop("checked", status);
	};

	publ.getCheckboxStatus = function() {
		return priv.rootElement.find('.tag_checkbox').is(":checked");
	};

	publ.getTagName = function() {
		return priv.rootElement.find(".tag_input").val();
	};

	return publ;
}