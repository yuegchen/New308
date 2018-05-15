$("select").append($("<option></option>")
    .attr("value", null));
$.each(stateNames, function(index, value) {
    $("select").append($("<option></option>")
        .attr("value", (index + 1))
        .text(value));
});

function setMode(evt, mode) {
    var i, tab_list, tab_buttons;

    // Highlight the button for the active mode, and display its div.
    tab_list = document.getElementsByClassName("panel_content");
    for (i = 0; i < tab_list.length; i++) {
        tab_list[i].style.display = "none";
    }
    buttons_list = document.getElementsByClassName("tab_button");
    for (i = 0; i < buttons_list.length; i++) {
        buttons_list[i].className = buttons_list[i].className.replace(" active", "");
    }
    document.getElementById(mode).style.display = "block";
    evt.currentTarget.className += " active";


    // Call the method to initialize the active tab
    switch (mode) {
        case "redistricting":
            //redistricting(1, null);
            break;
        case "manual":
            init_manual();
            break;
        case "compare":
            init_compare()
            break;
        case "visualize":
            break;
        default:
            break;
    }

    // ==============================================================================
    // ===== MANUAL MODE FUNCTIONS ==================================================
    // ==============================================================================

    function init_manual() {
        // Make sure the map is coloring by district affiliation
        setColoring(getColor_District);
        redraw();

        // Populate the div with content
        var ele = document.getElementById("district_container");
        ele.innerHTML = getDistrictDisplay();
    }

    function init_compare() {
        var div = $('<div></div>').addClass('cd-panel cd-panel--from-left js-cd-panel-main');

        var header = $('<header></header>').addClass('cd-panel__header');
        var h1 = $('<h1></h1>');
        var a = $('<a></a>').attr('href', '#');
        header.append(h1, a);

        var divContainer = $('<div></div>').addClass('cd-panel__container');
        var divContent = $('<div></div>').addClass('cd-panel__content');
        var table1 = $('<table></table>').addClass('table table-striped').attr('id', 'state-table1');
        var table2 = $('<table></table>').addClass('table table-striped').attr('id', 'state-table2');
        divContent.append(table1, table2);
        divContainer.append(divContent);

        div.append(header, divContainer);
    }

    function getDistrictDisplay() {
        var toReturn = "";
        var districtColors = getDistrictColors();
        var districts = Object.keys(districtColors);
        for (var i = 0; i < districts.length; i++) {
            toReturn += '<div class="district_selector" style="background-color:' + districtColors[districts[i]] + ';">';
            toReturn += '<tr>';
            toReturn += '<td><h5>District ' + districts[i] + '</h5></td>';
            toReturn += '<td><button class="select_button" id="dsb' + districts[i] + '"" onclick="panelSelectDistrict(' + districts[i] + ')">Select</button></td>';
            toReturn += '</tr>';
            toReturn += '</div>';
        }

        return toReturn;
    }

    function panelSelectDistrict(district) {
        setSelectedDistrict(district);

        // Mark active button
        buttons_list = document.getElementsByClassName("select_button");
        for (i = 0; i < buttons_list.length; i++) {
            buttons_list[i].className = buttons_list[i].className.replace(" active", "");
        }
        document.getElementById("dsb" + district).style.display.className += " active"
    }

    function compare(){
        var params = {
            stateId1: $('#select_state_1 option:selected').text();
            stateId2: $('#select_state_2 option:selected').text();
        }

        $.ajax({
            url: 'display/state-comparison',
            type: 'GET',
            dataType: 'json',
            success: function(response){

            }
            data: params
        })
    }
}