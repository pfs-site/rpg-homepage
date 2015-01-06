/*
 * This method handles comment form submits.
 * 
 * The comment data is posted to the server. If the request
 * is successful, the returned comment data is added as a
 * new comment to the comment form on the homepage.
 */
function addComment(commentData) {
	//Comment service returns comment representation
	var newComment = $('<article class="well well-large"/>');
	
	//Comment title
	var title = $('<h4>');
	title.text(commentData.author);
	newComment.append(title);
	
	//Date
	var date = $('<p>', {
		'class': 'badge badge-info'
	});
	date.text(new Date(commentData.date).toLocaleString());
	newComment.append(date);
	
	//Link
	if (commentData.link && commentData.link != "") {
		var link = $('<a>', {
			href: commentData.link
		});
		link.text(commentData.link);
		newComment.append(link);
	}
	
	//Text
	var text = $('<p>');
	text.text(commentData.text);
	newComment.append(text);
	
	//Append new comment to comment list
	$('#commentContainer').append(newComment).fadeIn();
}

$(document).ready(function() {
	var button = $("#comment-submit");
	
	button.click(function(ev) {
		var form = $("#website-comments-form");
		
		//Get entered values
		var comment = {
			author: form.find("#comment-author").val().trim(),
			link: form.find("#comment-link").val().trim(),
			text: form.find("#comment-text").val().trim()
		};
		
		//Check mandatory fields
		if (comment.author === "" || comment.text === "") {
			alert("Enter comment author and text. The link is optional.");
			return false;
		}
		
		//Post values to server
		var commentPostUrl = form.find("#comment-post-url").val();
		$.ajax(commentPostUrl, {
			contentType: 'application/json;charset=utf-8',
			data: JSON.stringify(comment),
			type: 'POST',
			success: addComment,
			dataType: 'json',
		});
		return false;
	});
});