var morrisLine;

function showMemeData(currentlySelectedMemeData) {
    var dataTimeSpan = currentlySelectedMemeData[currentlySelectedMemeData.length - 1].timeStamp - currentlySelectedMemeData[0].timeStamp;
    var dataLength = currentlySelectedMemeData.length;
    var maxSmile = -1;
    var smileData = [];
    for(var i = 0; i < currentlySelectedMemeData.length; i++) {
        if(currentlySelectedMemeData[i].smiling > maxSmile) {
            maxSmile = currentlySelectedMemeData[i].smiling;
        }

        smileData.push({
            'x' : "" + parseInt(i),
            'value' : (currentlySelectedMemeData[i].smiling == -1)? null : parseFloat(currentlySelectedMemeData[i].smiling)
        });
    }
    var selectedEmotion = currentlySelectedMemeData[0].selectedEmotion;

    $("#frame_slider").attr("max", currentlySelectedMemeData.length - 1);
    $("#frame_slider").val(0);
    $("#frame_slider").unbind("change mousemove");
    setTimeout(function() {$("#frame_slider").on("change mousemove", onSliderChange);}, 200);

    $("#meme_data_table").empty();

    $("#meme_data_table").append('<tr><th>Amount of Face Data</th><td>' + dataLength + '</td></tr>');
    // $("#meme_data_table").append('<tr><th>Data Time Span</th><td>' + dataTimeSpan + '</td></tr>');
    $("#meme_data_table").append('<tr><th>Selected Reaction</th><td>' + emotionCodes[selectedEmotion] + '</td></tr>');
    $("#meme_data_table").append('<tr><th>Max. Smile</th><td>' + maxSmile + '</td></tr>');

    $("#smile_data_chart_wrapper").empty();

    $("#smile_data_chart_wrapper").append('<div id="smile_data_chart" style="height: 250px"></div>');

    setTimeout(
        function() {
            morrisLine = new Morris.Line({
              // ID of the element in which to draw the chart.
              element: 'smile_data_chart',
              // Chart data records -- each entry in this array corresponds to a point on
              // the chart.
              data: smileData,
              // The name of the data record attribute that contains x-values.
              xkey: 'x',
              // A list of names of data record attributes that contain y-values.
              ykeys: ['value'],
              // Labels for the ykeys -- will be displayed when you hover over the
              // chart.
              labels: ['Value'],
              ymax: 1.0,
              ymin: 0.0,
              parseTime: false,
              continuousLine: false,
              smooth: false
            });
    }, 100);
}


function showFrameData(currentMemeDataIndex) {
    $("#frame_data_table").empty();

    $("#frame_data_table").append('<tr><th>frame num</th><td>' + currentMemeDataIndex + '</td></tr>');
    $("#frame_data_table").append('<tr><th>smiling</th><td>' + currentlySelectedMemeData[currentMemeDataIndex].smiling + '</td></tr>');
    $("#frame_data_table").append('<tr><th>Left Eye Open</th><td>' + currentlySelectedMemeData[currentMemeDataIndex].leftEyeOpen + '</td></tr>');
    $("#frame_data_table").append('<tr><th>Right Eye Open</th><td>' + currentlySelectedMemeData[currentMemeDataIndex].rightEyeOpen + '</td></tr>');
    $("#frame_data_table").append('<tr><th>timestamp</th><td>' + currentlySelectedMemeData[currentMemeDataIndex].timeStamp + '</td></tr>');

    if(morrisLine != undefined) {
      var elementWidth = morrisLine.elementWidth;
      var graphLeft = morrisLine.xStart;

      var currentHighlightX = graphLeft + currentMemeDataIndex * morrisLine.dx;
      var ratioOfWidth = currentHighlightX / elementWidth;

      var percentage = ratioOfWidth * 100;
      $("#smile_data_chart_wrapper").css("background", "linear-gradient(to right, white " + 
        (percentage - 0.6) + "%, #cc99cc " + 
        (percentage + 0.0) + "%, white " +  
        (percentage + 0.6) + "%)");
    }

    $("#frame_slider").val(currentMemeDataIndex);
}

function onSliderChange(e) {
    currentMemeDataIndex = $("#frame_slider").val();
    clearCanvas();
    showFace(currentMemeDataIndex);
    showFrameData(currentMemeDataIndex);
}