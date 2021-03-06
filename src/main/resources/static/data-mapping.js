

/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

//switches and thresholds
var TESTING = false;
var MAX_NUM_LABEL_CHARS = 20;


//data obtained from backend
var oc_list;
var oc_path = [];
var oc_labels = [];
var usr_list;

//mapping data
var mapping = {};//oc_label --> usr_label
var mapped_usr_item_ids = [];//[draggable_id]
var oc_usr_id_mapping = {};// #droppable_id --> draggable_id
var usr_label_mapping = {};// full-usr-label --> shortened-usr-label
var oc_label_mapping = {}; //full-oc-label --> shortened-oc-label

//tips
var oc_tip;
var usr_tip;


function find_oc_item(oc_label) {
    var found_oc_item = null;
    $('.oc-item').each(function (index, _oc_item) {
        var oc_item = $(_oc_item);
        var data_oc_label = oc_item.attr('data-oc-label');
        if (oc_label === data_oc_label) {
            found_oc_item = oc_item;
        }
    });

    return found_oc_item;
}

function find_usr_item(usr_label) {
    var found_usr_item = null;
    $('.usr-item').each(function (index, _usr_item) {
        var usr_item = $(_usr_item);
        var data_usr_label = usr_item.attr('data-usr-label');
        if (usr_label === data_usr_label) {
            found_usr_item = usr_item;
        }
    });

    return found_usr_item;
}

function map_usr_item_to_oc_item(oc_item, usr_item) {

    var oc_label = oc_item.attr('data-oc-label');
    var oc_id = oc_item.attr('id');
    var usr_label = usr_item.attr('data-usr-label');
    if (mapping[oc_label] == null) { // can be mapped
        mapping[oc_label] = usr_label;
        var usr_id = usr_item.attr('id');
        oc_usr_id_mapping[oc_id] = usr_id;
        var html = usr_label_mapping[usr_label];
        oc_item.html(html);
        usr_item.hide();
        mapped_usr_item_ids.push(usr_id);
    }
}

function build_mapping() {
    $.ajax({
        url: baseApp + "/submission/current-mapping",
        type: "GET",
        timeout: 0,
        success: function (_mapping) {
            for (var oc_label in _mapping) {
                var usr_label = _mapping[oc_label];
                var oc_item = find_oc_item(oc_label);
                var usr_item = find_usr_item(usr_label);
                if (oc_item !== null && usr_item !== null) {
                    map_usr_item_to_oc_item(oc_item, usr_item);
                }
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
    makeProgressSectionVisible(false);
}

function build_filters() {
    $('#oc-filter-input').keyup(function () {
        var query = $(this).val().toLowerCase();
        if (query == '') {
            $('.oc-label-item').each(function (index, obj) {
                $(obj).show();
                $('#oc_' + index).show();
            });
        }
        else {
            $('.oc-label-item').each(function (index, obj) {
                var item = $(obj);
                var label = item.attr('data-oc-label').toLowerCase();
                if (label.indexOf(query) > -1) {
                    item.show();
                    $('#oc_' + index).show();
                }
                else {
                    item.hide();
                    $('#oc_' + index).hide();
                }
            });
        }
    });

    $('#usr-filter-input').keyup(function () {
        var query = $(this).val().toLowerCase();
        if (query == '') {
            $('.usr-item').each(function (index, obj) {
                var usr_id = $(obj).attr('id');
                if (mapped_usr_item_ids.indexOf(usr_id) == -1) $(obj).show();
            });
        }
        else {
            $('.usr-item').each(function (index, obj) {
                var item = $(obj);
                var usr_id = item.attr('id');
                if (mapped_usr_item_ids.indexOf(usr_id) == -1) {
                    var label = item.attr('data-usr-label').toLowerCase();
                    if (label.indexOf(query) > -1) {
                        item.show();
                    }
                    else {
                        item.hide();
                    }
                }
            });
        }
    });

}

function build_tips() {
    oc_tip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);
    usr_tip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    var tip_style = 'font-size:18px; font-family: Georgia; font-weight: bold;';

    d3.selectAll('.oc-label-item').each(function () {
        var div = d3.select(this);
        div.on('mouseover', function () {
            var content = div.html();
            var oc_label = div.attr('data-oc-label');

            if (content !== '' && oc_label.length > MAX_NUM_LABEL_CHARS) {
                var box = div[0][0].getBoundingClientRect();
                oc_tip
                    .transition().duration(200)
                    .style('opacity', .95);
                oc_tip
                    .html('<span style=\"' + tip_style + '\">' + oc_label + '</span>');

                var tipBox = oc_tip[0][0].getBoundingClientRect();
                var top = +box.top - 3;
                var left = +box.right - tipBox.width - 6;

                oc_tip
                    .style('top', top + 'px')
                    .style('left', left + 'px');
            }
        });
        div.on('mouseout', function () {
            oc_tip
                .transition().duration(200)
                .style('opacity', 0);
        });
    });

    d3.selectAll('.oc-item').each(function () {
        var div = d3.select(this);
        div.on('mouseover', function () {
            var content = div.html();
            var oc_label = div.attr('data-oc-label');
            var usr_label = mapping[oc_label];

            if (content !== '' && usr_label.length > MAX_NUM_LABEL_CHARS) {
                var box = div[0][0].getBoundingClientRect();
                var top = +box.top - 3;
                var left = +box.left;
                usr_tip
                    .transition().duration(200)
                    .style('opacity', .95);

                usr_tip
                    .html('<span style=\"' + tip_style + '\">' + usr_label + '</span>')
                    .style('top', top + 'px')
                    .style('left', left + 'px');
            }
        });
        div.on('mouseout', function () {
            usr_tip
                .transition().duration(200)
                .style('opacity', 0);
        });
    });

    d3.selectAll('.usr-item').each(function () {
        var div = d3.select(this);
        div.on('mouseover', function () {
            var content = div.html();
            var usr_label = div.attr('data-usr-label');

            if (content !== '' && usr_label.length > MAX_NUM_LABEL_CHARS) {
                var box = div[0][0].getBoundingClientRect();
                var top = +box.top - 3;
                var left = +box.left - 10;
                usr_tip
                    .transition().duration(200)
                    .style('opacity', .95);

                usr_tip
                    .html('<span style=\"' + tip_style + '\"\>' + usr_label + '</span>')
                    .style('top', top + 'px')
                    .style('left', left + 'px');
            }
        });
        div.on('mouseout', function () {
            usr_tip
                .transition().duration(200)
                .style('opacity', 0);
        });
    });
}

function clear_mapping_item(oc_id) {
    if (oc_usr_id_mapping[oc_id] !== null) {
        $('#' + oc_id).html('');
        var oc_label = $('#' + oc_id).attr('data-oc-label');
        mapping[oc_label] = null;
        var usr_id = oc_usr_id_mapping[oc_id];
        $('#' + usr_id).show();
        oc_usr_id_mapping[oc_id] = null;
        mapped_usr_item_ids.splice($.inArray(usr_id, mapped_usr_item_ids), 1);
    }
}

function clear_mapping() {
    for (var oc_id in oc_usr_id_mapping) {
        clear_mapping_item(oc_id);
    }
}

function match_items() {
    clear_mapping();
    $('.usr-item').each(function () {
        var usr_obj = $(this);
        var usr_label = usr_obj.attr('data-usr-label');
        var usr_id = usr_obj.attr('id');
        $('.oc-item').each(function () {
            var oc_obj = $(this);
            var oc_label = oc_obj.attr('data-oc-label');
            var oc_id = oc_obj.attr('id');
            if (usr_label == oc_label) {
                mapping[oc_label] = usr_label;
                oc_usr_id_mapping[oc_id] = usr_id;
                var short_usr_label = usr_label_mapping[usr_label];
                oc_obj.html(short_usr_label);
                usr_obj.hide();
                mapped_usr_item_ids.push(usr_id);
            }
        });
    });
}

function generate_mapping() {
    var output = [];
    var eventName = oc_path[0];
    var crfName = oc_path[1];
    var crfVersion = oc_path[2];

    for (var oc_label in mapping) {
        var usr_label = mapping[oc_label];
        if (usr_label !== null) {
            var matching = {};
            matching['study'] = '';
            matching['eventName'] = eventName;
            matching['crfName'] = crfName;
            matching['crfVersion'] = crfVersion;
            matching['ocItemName'] = oc_label;//'Open Clinica Item'
            matching['usrItemName'] = usr_label;//'User Defined Item'
            output.push(matching);
        }
    }
    return output;
}

function export_mapping() {
    var output = generate_mapping();
    if (output.length > 0) {
        var str = JSON.stringify(output);
        var blob = new Blob([str], {type: "application/json"});
        saveAs(blob, "mapping.json");
    }
}

function update_submission() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        timeout: 0,
        data: {step: "feedback-data"},
        success: function () {
            window.location.href = baseApp + "/views/feedback-data";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
}

function upload_mapping() {
    var output = generate_mapping();
    if (output.length > 0) {
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            url: baseApp + "/upload/mapping",
            type: "POST",
            timeout: 0,
            dataType: 'json',
            data: JSON.stringify(output),
            success: function () {
                update_submission();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            }
        });
    }
    else {
        update_submission();
    }
}

function shorten_label(label) {
    var slabel = label;
    if (slabel.length > MAX_NUM_LABEL_CHARS) {
        slabel = slabel.substring(0, MAX_NUM_LABEL_CHARS);
        slabel += '...';
    }
    return slabel;
}

function callback_build_usr_list(data) {
    usr_list = data;

    for (var i = 0; i < usr_list.length; i++) {
        var usr_label = usr_list[i];
        var short_usr_label = shorten_label(usr_label);
        usr_label_mapping[usr_label] = short_usr_label;
        var usr_id = 'usr_' + i;
        var html = '<div id=\"' + usr_id + '\" class="label label-info usr-item ui-widget-content" data-usr-label=\"' + usr_label + '\">' + short_usr_label + '</div>';
        $('#right-col-area').append(html);
        $('#' + usr_id).draggable({
            // revert: true,
            helper: 'clone',
            appendTo: 'body'
        });
    }

    //initialize mapping
    for (var i = 0; i < oc_labels.length; i++) {
        var oc_item_label = oc_labels[i];
        mapping[oc_item_label] = null;
        var oc_id = 'oc_' + i;
        oc_usr_id_mapping[oc_id] = null;
    }

    //build tips and filters
    build_tips();
    build_filters();
    build_mapping();
}

function build_usr_list() {
    if (!TESTING) {
        $.ajax({
            url: baseApp + "/submission/user-items",
            type: "GET",
            timeout: 0,
            success: callback_build_usr_list,
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            }
        });
    }
    else {
        callback_build_usr_list(usr_list);
    }
}

function handle_usr_item_drop(event, ui) {
    var oc_item = $(this);
    var usr_item = ui.draggable;
    map_usr_item_to_oc_item(oc_item, usr_item);
}

function handle_oc_item_click() {
    var oc_id = $(this).attr('id');
    clear_mapping_item(oc_id);
}

function callback_build_oc_list(data) {
    oc_list = data;

    var path_items = data[0].split('\\');
    oc_path.push(path_items[1]);//event
    oc_path.push(path_items[2]);//crf
    oc_path.push(path_items[3]);//crf version
    //<span class="label label-warning">Warning</span>
    var study_span = '<span class="label label-warning">' + path_items[0] + '</span>';
    var event_span = '<span class="label label-warning">' + path_items[1] + '</span>';
    var crf_span = '<span class="label label-warning">' + path_items[2] + '</span>';
    var crfv_span = '<span class="label label-warning">' + path_items[3] + '</span>';

    var path_html = 'Study: ' + study_span + ' &#8594; Event: ' + event_span + ' &#8594; CRF: ' + crf_span + ' &#8594; CRF version: ' + crfv_span;
    $('#path-area').html('<h4><div>' + path_html + '</div></h4><br>');

    for (var i = 0; i < oc_list.length; i++) {
        var arr = oc_list[i].split('\\');
        var oc_label = arr[4];
        var short_oc_label = shorten_label(oc_label);
        oc_label_mapping[oc_label] = short_oc_label;
        oc_labels.push(oc_label);

        //append html
        var html = '<div class="label label-primary oc-label-item" data-oc-label=\"' + oc_label + '\">' + short_oc_label + '</div><hr>';
        $('#oc-label-col-area').append(html);

        var oc_id = 'oc_' + i;
        html = '<div id=\"' + oc_id + '\" class="oc-item ui-widget-header" data-oc-label=\"' + oc_label + '\"></div><hr>';
        $('#oc-col-area').append(html);

        //add droppable behavior
        $('#' + oc_id).droppable({
            tolerance: 'pointer',
            hoverClass: "label-warning",
            drop: handle_usr_item_drop
        });
        $('#' + oc_id).click(handle_oc_item_click);
    }

    build_usr_list();
}


function build_oc_list() {
    if (!TESTING) {
        $.ajax({
            url: baseApp + "/metadata/targetedCrf",
            type: "GET",
            timeout: 0,
            success: callback_build_oc_list,
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            }
        });
    }
    else {
        callback_build_oc_list(oc_list);
    }
}

function test() {
    if (TESTING) {
        oc_list = [];
        usr_list = [];
        var delim = '\\';
        var head = 'event' + delim + 'crf' + delim + 'version' + delim;

        oc_list.push(head + 'long_long_long_long_long_long_long_long_long_OC_label');
        for (var i = 0; i < 100; i++) {
            var oclabel = head + 'oc_label_' + i;
            oc_list.push(oclabel);
        }

        usr_list.push('OC_Label_3');
        usr_list.push('oc_label_5');
        usr_list.push('a_Very_loooooooooooooooooooooooooooong_label');
        usr_list.push('a_Very_looooooooppppppppppp');
        usr_list.push('a_Very_loooooooopppppppppp');
        usr_list.push('XXXXXXXXXXXXXXXXXXXXXXXXXX');//26 Xs
        usr_list.push('XXXXXXXXXXXXXXXXXXXXXXXXX');//25 Xs
        usr_list.push('XXXXXXXXXXXXXXXXXXXXXXXXXXX');//27 Xs
        usr_list.push('XXXXXX_XPQXXXXXXXXXXXXXXX');//25 Xs
        usr_list.push('xxxxxxxxxxxxxxxxxxxxxxxxx');//25 Xs

        for (var i = 0; i < 50; i++) {
            var usrlabel = 'usr_label_' + i;
            usr_list.push(usrlabel);
        }
    }
}


function init() {
    _SESSION_CONFIG = JSON.parse(localStorage.getItem("session_config"));
    _CURRENT_SESSION_NAME = localStorage.getItem("current_session_name");

    test();
    build_oc_list();

    //disable "Apply the mapping file" button if MAPPING_FILE_ENABLED (config.js) is false
    if (!_SESSION_CONFIG[_CURRENT_SESSION_NAME]['MAPPING_FILE_ENABLED']) {
        $('#apply-map-btn').hide();
    }
}

function stepback() {
    window.location.href = baseApp + "/views/data";
}


function makeProgressSectionVisible(visible) {
    if (visible === true) {
        document.getElementById('progression-section').style.display = 'inline';
        document.getElementById('mapping-area').style.display = 'none';
        document.getElementById('control-container').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'none';
        document.getElementById('mapping-area').style.display = 'inline';
        document.getElementById('control-container').style.display = 'inline';
    }
}

$(document).ready(function () {
    makeProgressSectionVisible(true);
    init();

    $('#auto-map-btn').click(match_items);
    $('#clear-map-btn').click(clear_mapping);
    $('#export-map-btn').click(export_mapping);
    $('#map-proceed-btn').click(upload_mapping);
    $('#map-back-btn').click(stepback);
});
