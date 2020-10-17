jQuery.fn.exists = function(){return this.length>0;}

var metadata;
var buyPriceLookup = {};
var sellPriceLookup = {};
var counts = {};
var gwuCounts = {};
var unlocks = {};
var imageMap = {};
var acquisitionMethods = [
    { id : "tp", name : "Trading Post", hideOnIcon : true, category : "Standard Acquisition"},
	{ id : "vendor", name : "Vendor", category : "Standard Acquisition", hideOnIcon : true},
	{ id : "gold", name : "Gold", category : "Standard Currency", hideOnIcon : true},
	{ id : "karma", name : "Karma", category : "Standard Currency"},
    { id : "craft", name : "Crafting", category : "Standard Acquisition"},
	{ id : 'gem', name : "Gem Store", category : "Black Lion"},
	{ id : "laurel", name : "Laurel", category : "Standard Currency"},
	{ id : "story", name : "Story Reward", category : "Standard Acquisition"},
	{ id : "achievement", name : "Achievement", category : "Standard Acquisition"},
	{ id : "globofectoplasm", name : "Glob of Ectoplasm", category : "Standard Acquisition", hideOnIcon : true},
	{ id : "karkashell", name : "Karka Shell", category : "Standard Acquisition", hideOnIcon : true},
    { id : "boh", name : "Badge of Honor", category : "World vs World"},
	{ id : "fractalrelic", name : "Fractal Relic", category : "Fractals"},
	{ id : "pristinefractalrelic", name : "Pristine Fractal Relic", category : "Fractals"},
	{ id : "fractalresearchpage", name : "Fractal Research Page", category: "Fractals"},
  { id : "fractaljournal", name : "Fractal Journal", category: "Fractals"},
	{ id : "goldenfractalrelic", name : "Golden Fractal Relic", category : "Fractals"},
	{ id : "integratedmatrix", name : "Integrated Fractal Matrix", category : "Fractals"},
	{ id : "unstablefractalessence", name : "Unstable Fractal Essence", category : "Fractals"},
	{ id : "spiritshard", name : "Spirit Shard", category : "Standard Currency", hideOnIcon : true},
	{ id : "guildcommendation", name : "Guild Commendation", category : "Standard Currency"},
	{ id : "event", name : "Event", category : "Standard Acquisition"}, 
	{ id : "pvp", name : "Reward Track", category : "Player vs Player"},
	{ id : "pvplt", name : "PvP League Ticket", category : "Player vs Player"},
	{ id : "pvptournament", name : "PvP Automated Tournament", category : "Player vs Player"},
	{ id : "wvwsct", name : "WvW Skirmish Claim Ticket", category : "World vs World"},
	{ id : "memoryofbattle", name : "Memory of Battle", category : "World vs World"},
	{ id : 'deluxe', name : "Deluxe Edition", category : "Purchase"},
    { id : 'hallofmonuments', name : "Hall of Monuments (GW1)", category : "Purchase"},
    { id : 'blacklionchest', name : "Black Lion Chest", category : "Black Lion"},
	{ id : "bls", name : "Black Lion Statuette", category : "Black Lion"},
	{ id : "gwu", name : "Guaranteed Wardrobe Unlock", hideOnIcon : true, category : "Black Lion"},
    { id : 'blt',  name : "Black Lion Ticket", category : "Black Lion"},
    { id : "blmt", name : "Black Lion Miniature Claim Ticket", category : "Black Lion"},
	{ id : "blsprocket", name : "Black Lion Commemorative Sprocket", category : "Black Lion"},
	{ id : "ascaloniantear", name : "Ascalonian Tear", category : "Dungeon"},
	{ id : "deadlybloom", name : "Deadly Bloom", category : "Dungeon"},
	{ id : "sealofbeetletun", name : "Seals of Beetletun", category : "Dungeon"},
	{ id : "manifesto", name : "Manifesto of the Moletariate", category : "Dungeon"},
	{ id : "charrcarving", name : "Flame Legion Charr Carving", category : "Dungeon"},
	{ id : "knowledgecrystal", name : "Knowledge Crystal", category : "Dungeon"},
	{ id : "symbolofkoda", name : "Symbol of Koda", category : "Dungeon"},
	{ id : "shardofzhaitan", name : "Shard of Zhaitan", category : "Dungeon"},
	{ id : "strike", name : "Strike Mission", category : "Strike Mission"},
	{ id : "bauble", name : "Bauble Bubbles", category : "Seasonal Event"},
    { id : "bundleofloot", name : "Bundle Of Loot", category : "Seasonal Event"},
	{ id : "festivaltoken", name : "Festival Token", category : "Seasonal Event"},
	{ id : "favorofthefestival", name : "Favor of the Festival", category : "Seasonal Event"},	
	{ id : 'banditcrest', name : "Bandit Crest", category : "Living Story 2"},
    { id : "candycorncob", name : "Candy Corn Cob", category : "Seasonal Event"},
	{ id : "snowdiamond", name : "Snow Diamond", category : "Seasonal Event"},
	{ id : "enchantedsnowball", name : "Enchanted Snowball", category : "Seasonal Event"},
	{ id : "snowflake", name : "Snowflake", category : "Seasonal Event"},	
	{ id : "jorbreaker", name : "Jorbreaker", category : "Seasonal Event"},
    { id : "birthday1", name : "First Birthday", category : "Birthday"},
    { id : "birthday3", name : "Third Birthday", category : "Birthday"},
    { id : "birthday4", name : "Forth Birthday", category : "Birthday"},
    { id : "birthday5", name : "Fifth Birthday", category : "Birthday"},
	{ id : "birthday6", name : "Sixth Birthday", category : "Birthday"},
	{ id : "birthday7", name : "Seventh Birthday", category : "Birthday"},
  { id : "birthday8", name : "Eighth Birthday", category : "Birthday"},
    { id : "airshippart", name : "Airship Part", category : "Heart of Thorns"},
	{ id : "aurillium", name : "Lump of Aurillium", category : "Heart of Thorns"},
	{ id : "leylinecrystal", name : "Ley Line Crystal", category : "Heart of Thorns"},
	{ id : "chakegg", name : "Chak Egg", category : "Heart of Thorns"},
    { id : "reclaimedplate", name : "Reclaimed Metal Plate", category : "Heart of Thorns"},
	{ id : "adventure", name : "Adventure", category : "Standard Acquisition"},
	{ id : "crystallineore", name : "Crystalline Ore", category : "Heart of Thorns"},
	{ id : "provisionertoken", name : "Provisioner Token", category : "Heart of Thorns"},
	{ id : "magnetiteshard", name : "Magnetite Shard", category : "Raid"},
	{ id : "gaeting", name : "Gaeting Crystal", category : "Raid"},
	{ id : "unboundmagic", name : "Unbound Magic", category : "Living Story 3"},
	{ id : "bloodruby", name : "Blood Ruby", category : "Living Story 3"},
    { id : "petrifiedwood", name : "Petrified Wood", category : "Living Story 3"},
	{ id : "winterberries", name : "Fresh Winterberry", category : "Living Story 3"},
    { id : "jadeshard", name : "Jade Shard", category : "Living Story 3"},
	{ id : "fireorchidblossom", name : "Fire Orchid Blossom", category : "Living Story 3"},
    { id : "orrianpearl", name : "Orrian Pearl", category : "Living Story 3"},
    { id : "tradecontract", name : "Trade Contract", category : "Path of Fire"},
	{ id : "casinocoin", name : "Casino Coin", category : "Path of Fire"},
	{ id : "shadowsseal", name : "Order of Shadows Seal", category : "Path of Fire"},
	{ id : "elegymosaic", name : "Elegy Mosaic", category : "Path of Fire"},
	{ id : "volatilemagic", name : "Volatile Magic", category : "Living Story 4"},
	{ id : "kralkatiteore", name : "Kralkatite Ore", category : "Living Story 4"},
	{ id : "difluorite", name : "Difluorite Crystal", category : "Living Story 4"},
	{ id : "inscribedshard", name : "Inscribed Shard", category : "Living Story 4"},
	{ id : "brandedmass", name : "Branded Mass", category : "Living Story 4"},
	{ id : "mistbornmote", name : "Mistborn Mote", category : "Living Story 4"},
	{ id : "swimspeedinfusion", name : "Swim-speed Infusion", category : "Living Story 4"},
    { id : "mistonium", name : "Lump of Mistonium", category : "Living Story 4"},
	{ id : "racingmedallion", name : "Racing Medallion", category : "Living Story 4"},
	{ id : "hatchedchili", name : "Hatched Chili", category : "The Icebrood Saga"},
    { id : "eternaliceshard", name : "Eternal Ice Shard", category : "The Icebrood Saga"},
	{ id : "eitriteingot", name: "Eitrite Ingot", category : "The Icebrood Saga"},
	{ id : "shardofglory", name : "Shards of Glory", category : "Player vs Player"},
	{ id : "ascendedshardofglory", name : "Ascended Shards of Glory", category : "Player vs Player"},
	{ id : "grandmasterartifactmark", name : "Grandmaster Artificer's Mark", hideOnIcon : true, category : "Craftable"},
	{ id : "grandmasterweaponmark", name : "Grandmaster Weaponsmith's Mark", hideOnIcon : true, category : "Craftable"},
	{ id : "grandmasterhuntsmanmark", name : "Grandmaster Huntsman's Mark", hideOnIcon : true, category : "Craftable"},
	{ id : "grandmastertailorsmark", name : "Grandmaster Tailor's Mark", hideOnIcon : true, category : "Craftable"},
	{ id : "grandmasterleatherworkersmark", name : "Grandmaster Leatherworker's Mark", hideOnIcon : true, category : "Craftable"},
	{ id : "grandmasterarmorsmithsmark", name : "Grandmaster Armorsmith's Mark", hideOnIcon : true, category : "Craftable"}];
	
var acquisitionMethodsLookup = {}
for (method of acquisitionMethods) { acquisitionMethodsLookup[method.id] = method }
			
function gwuFilter(x) {return x.sources.includes('gwu')}			
			
function setupMenuItems() {
	$('.nav li').click(function() {
		$('.page').toggleClass('hidden', true);
		$('.nav a').toggleClass('active', false);
		var link = $(this).find('a');
		link.toggleClass('active', true);
		$(link.attr('href')).toggleClass('hidden', false);
	});
}

function setupThresholdCalculator() {
	$('#min-gold-value').change(updateThresholdCalculation);
	$('#min-price-type').change(updateThresholdCalculation);
	$('#analyse-selection').change(updateThresholdCalculation);
}

function updateThresholdCalculation() {
	var itemLookup;
	if ($('#min-price-type')[0].value == 'buy') {
		itemLookup = buyPriceLookup;
	} else {
		itemLookup = sellPriceLookup;
	}
	var allItems = ($('#analyse-selection')[0].value == 'all')
	console.log('allItems ' + allItems)
	
	$("div[id*=list-entry-]").remove();
	
	var threshold = $('#min-gold-value')[0].value * 10000;
	var totalCost = 0;
	var total = 0;
	var items = [];
	for (var section in itemLookup) {
		if (itemLookup.hasOwnProperty(section)) {
			var sectionItems = itemLookup[section];
			for (var itemId in sectionItems) {
				if (sectionItems.hasOwnProperty(itemId) && !isUnlocked(section, itemId)) {
					var data = sectionItems[itemId];
					if (!isNaN(data.price) && data.price < threshold && (allItems || data.gwu)) {
						data.section = section;
						totalCost += data.price;
						total++;
						items.push(data);
					}
				}
			}
		}
	}
	
	items.sort(function (a, b) {
		return a.price - b.price;
	});

	var list = '';
    for (var item of items) {
		var gold = Math.floor(item.price / 10000);
		var silver = Math.floor(item.price / 100) % 100;
		var copper = item.price % 100;
		list += '<div class="entry" id="list-entry-' + item.section + '-' + item.details.id + '"><div class="entry-checkbox"><input type="checkbox" /></div>'
			  + '<div class="entry-name">' + item.name + '</div><div class="entry-price"><span>' + gold + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span>'
		      + ' <span id="min-unlock-silver">' + silver + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span>'
			  + ' <span id="min-unlock-copper">' + copper + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></div></div>';
	}	
	
	$('#min-unlock-gold').text(Math.floor(totalCost / 10000));
	$('#min-unlock-silver').text(Math.floor(totalCost / 100) % 100);
	$('#min-unlock-copper').text(totalCost % 100);
	$('#min-total').text(total);
	$('#analyse-list').append(list);
	
	for (var item of items) {
		$('#list-entry-' + item.section + '-' + item.details.id).click(function(item) {
			showDetails(item, 'analyse-');
		}.bind(null, item.details));
	}
	updateSectionFolding();
}

function isUnlocked(section, id) {
	return $('#' + section + '-' + id).hasClass('unlocked');
}

function storageAvailable(type) {
	try {
		var storage = window[type],
			x = '__storage_test__';
		storage.setItem(x, x);
		storage.removeItem(x);
		return true;
	}
	catch(e) {
		return false;
	}
}

function addAcquisitionFilters() {
    var acquisitionDetails = $('#advanced-filter-section')
	var filterSections = {};

	for (method of acquisitionMethods) {
		var section = filterSections[method.category];
		if (section == null) {
			section = '<div class="filter-group"><h3>' + method.category + '</h3>';
		}
		section += '<div class="filter-option">';
		section += '<span class="base-icon ' + method.id + '-icon" role="img" aria-label="' + method.name + '"></span>';
		section += '<div class="filter-label">' + method.name + '</div>';
		section += '<div class="filter-selection-div"><select id="filter-' + method.id + '" name="filter-' + method.name + '" class="filter-selection"><option value="ignore"></option><option value="include" data-id="' + method.id + '">Include</option><option value="exclude" data-id="' + method.id + '">Exclude</option></select></div></div>';
		filterSections[method.category] = section;
	}
	var filterContent = "<details open='true'><summary class='advanced-filter-summary'>Advanced Filters...</summary>";
	Object.keys(filterSections).forEach(function(key,index) {
		filterContent += filterSections[key];
		filterContent += '</div>';
	});
	filterContent += "</details>";
	acquisitionDetails.append(filterContent);
	var filter = $('.filter-selection').change(function() {
		updateFilter($('#filter-by-acquisition')[0].value, $('#gwu-toggle')[0].checked);
	});
	
}

function addAcquisitionDetails(prefix) {
    var acquisitionDetails = $('#' + prefix + 'selection-acquisition-methods')
    for (method of acquisitionMethods) {
        acquisitionDetails.append('<div id="' + prefix + 'acquisition-' + method.id + '"><span class="base-icon ' + method.id + '-icon" role="img" aria-label="' + method.name + '"/></span>' + method.name)
    }
}

function buildSite(data) {
	
	$('.lds-container').remove()
	
	metadata = data;
	for (o of metadata.images) {
		imageMap[o.name] = o.image;
	}
	setupMenuItems();
	buildSections();
	updateCounts();
	setupThresholdCalculator();
	addAcquisitionFilters();
    addAcquisitionDetails('');
	addAcquisitionDetails('analyse-');
		
	$('#api-key').change(function(event) {
		event.preventDefault();
		key = $('#api-key').val().trim()
		if (key.length == 72) {
			if (storageAvailable('localStorage')) {
				localStorage.setItem('key', key); 
			}
			filterWithApiKey(key);
		} else {
			if (storageAvailable('localStorage')) {
				localStorage.removeItem('key'); 
			}
			clear();
		}
	});
	
	$('#clear-unlocks').submit(function(event) {
		event.preventDefault();
		$('#api-error').toggleClass('hidden', true);
		clear();
	}.bind(this));
	
	$('#filter-by-acquisition')[0].value = 'all';
	$('#filter-by-acquisition').change(function() {
		updateFilter(this.value, $('#gwu-toggle')[0].checked);
	});
	
	$('#rendering-mode-selection')[0].value = 'icon';
	$('#rendering-mode-selection').change(function() {
		if (this.value == 'icon') {
			$('#rendering-mode').attr('href', 'css/icon-mode.css');
		} else if (this.value == 'text'){
			$('#rendering-mode').attr('href', 'css/text-mode.css');
		}
	});
	
	$('#gwu-toggle').change(function() {
		updateFilter($('#filter-by-acquisition')[0].value, this.checked);
	});
	
	if (storageAvailable('localStorage')) {
		if (localStorage.getItem('key')) {
			var key = localStorage.getItem('key');
			$('#api-key').val(key); 
			filterWithApiKey(key);
		} 
		var gwuOnly = false;
		var filter = 'all';
		if (localStorage.getItem('gwu-only')) {
			gwuOnly = localStorage.getItem('gwu-only') == 'true';
		}
		if (localStorage.getItem('filter')) {
			filter = localStorage.getItem('filter');
		}
		$('#filter-by-acquisition')[0].value = filter;
		$('#gwu-toggle')[0].checked = gwuOnly;
		updateFilter(filter, gwuOnly)
	}
}

function updateFilter(displayMode, gwuOnly) {
	$('.item').toggleClass('hidden', false);
	$('.section-groups').toggleClass('hidden', displayMode == 'tp-buy' || displayMode == 'tp-sell' || displayMode == 'buy' || displayMode == 'sell');
	$('.tp-buy-sorted').toggleClass('hidden', displayMode != 'tp-buy' && displayMode != 'buy');
	$('.tp-sell-sorted').toggleClass('hidden', displayMode != 'tp-sell' && displayMode != 'sell');
	$('#advanced-filter-section').toggleClass('hidden', displayMode != 'advanced');
	
	if (displayMode == 'setbuy') {
	    sortGroupsByBuyTotal();
	} else if (displayMode == 'setsell') {
		sortGroupsBySellTotal();
	} else {
		sortGroupsByName();
	}
	
	if (displayMode == 'tp-buy' || displayMode == 'tp-sell') {
		$('.item').not('.tp').toggleClass('hidden', true);
	}
	if (displayMode == 'gold' || displayMode == 'setbuy' || displayMode == 'setsell') {
		$('.item').not('.gold').toggleClass('hidden', true);
	} else if (displayMode == 'karma') {
		$('.item').not('.karma').toggleClass('hidden', true);
	} else if (displayMode == 'craft') {
		$('.item').not('.craft').toggleClass('hidden', true);
	} else if (displayMode == 'boh') {
		$('.item').not('.boh').toggleClass('hidden', true);
	} else if (displayMode == 'ls3') {
		$('.item').not('.ls3').toggleClass('hidden', true);
	} else if (displayMode == 'ls4') {
		$('.item').not('.ls4').toggleClass('hidden', true);
  } else if (displayMode == 'ibs') {
    $('.item').not('.ibs').toggleClass('hidden', true);
	} else if (displayMode == 'other') {
		$('.item.gold').toggleClass('hidden', true);
		$('.item.karma').toggleClass('hidden', true);
		$('.item.craft').toggleClass('hidden', true);
		$('.item.boh').toggleClass('hidden', true);
		$('.item.ls3').toggleClass('hidden', true);
		$('.item.ls4').toggleClass('hidden', true);
    $('.item.ibs').toggleClass('hidden', true);
	} else if (displayMode == 'advanced') {
		processAdvancedFilter();
	}
	if (gwuOnly == true) {
		$('.item').not('.gwu').toggleClass('hidden', true);
		$('#totals').toggleClass('hidden', true);
		$('#gwu-totals').toggleClass('hidden', false);
		$('.section-counts').toggleClass('hidden', true);
		$('.gwu-section-counts').toggleClass('hidden', false);
	} else {
		$('#totals').toggleClass('hidden', false);
		$('#gwu-totals').toggleClass('hidden', true);
		$('.section-counts').toggleClass('hidden', false);
		$('.gwu-section-counts').toggleClass('hidden', true);
	}
	updateGroupVisibility();
	updateCounts();
	updateSectionFolding();
	if (storageAvailable('localStorage')) {
		localStorage.setItem('gwu-only', gwuOnly); 
		localStorage.setItem('filter', displayMode);
	}
}

function sortGroupsByBuyTotal() {
	$('.section-groups').each(function (index, section) {
		$(section.children).sort(function (a, b) { return parseInt(a.dataset.buyTotal) - parseInt(b.dataset.buyTotal); }).appendTo(section)
	});
	
}

function sortGroupsBySellTotal() {
	$('.section-groups').each(function (index, section) {
		$(section.children).sort(function (a, b) { return parseInt(a.dataset.sellTotal) - parseInt(b.dataset.sellTotal); }).appendTo(section)
	});
	
}

function sortGroupsByName() {
	$('.section-groups').each(function (index, section) {
		$(section.children).sort(function (a, b) { return a.dataset.ordering - b.dataset.ordering; }).appendTo(section)
	});
}

function processAdvancedFilter() {
	$('.item').toggleClass('hidden', true);
	$(".filter-selection option:selected[value='include']").each(function (index, filter) { 
		$('.item.' + $(filter).data("id")).toggleClass('hidden', false);
	});
	$(".filter-selection option:selected[value='exclude']").each(function (index, filter) { 
		$('.item.' + $(filter).data("id")).toggleClass('hidden', true);
	});
}

function filterWithApiKey(key) {
	for (var sectionIndex = 0; sectionIndex < metadata.items.length; ++sectionIndex) {
		var sectionData = metadata.items[sectionIndex];
		if (sectionData.unlockUrl) {
			$.ajax({
				url: sectionData.unlockUrl + '?access_token=' + key, 
				dataType: 'json',
				success: function(sectionData, result) {
					$('#api-error').toggleClass('hidden', true);
					unlocks[sectionData.id] = result;
					var section = $('#' + sectionData.id + '-section');
					var contentSection = section.children('.section-groups');
					var buySection = section.children('.tp-buy-sorted');
					var sellSection = section.children('.tp-sell-sorted');
					contentSection.find('.item').toggleClass('unlocked', false);
					buySection.find('.item').toggleClass('unlocked', false);
					sellSection.find('.item').toggleClass('unlocked', false);
					
					var unlockedBuyValue = 0;
					var unlockedSellValue = 0;
					var unlockedCount = 0;
					var gwuUnlockedBuyValue = 0;
					var gwuUnlockedSellValue = 0;
					var gwuUnlockedCount = 0;
					var sectionBuyPrices = buyPriceLookup[sectionData.id];
					var sectionSellPrices = sellPriceLookup[sectionData.id];
												
					for (var i = 0; i < result.length; ++i) {
						var id = result[i];
						if (typeof id === 'object') {
							if (!id.permanent) {
								continue;
							}
							id = id.id;
						}
						var icon = $('#' + sectionData.id + '-' + id);
						if (icon.exists()) {
							unlockedCount++;
							icon.toggleClass('unlocked', true);
							if (icon.hasClass('gwu')) {
								gwuUnlockedCount++;
							}
						}
						var buyicon = $('#' + sectionData.id + '-buy-tp-' + id).toggleClass('unlocked', true);
						var sellicon = $('#' + sectionData.id + '-sell-tp-' + id).toggleClass('unlocked', true);
						if (sectionBuyPrices[id]) {
							unlockedBuyValue += sectionBuyPrices[id].price;
							if (icon.hasClass('gwu')) {
								gwuUnlockedBuyValue += sectionBuyPrices[id].price;
							}
						}
						if (sectionSellPrices[id]) {
							unlockedSellValue += sectionSellPrices[id].price;
							if (icon.hasClass('gwu')) {
								gwuUnlockedSellValue += sectionSellPrices[id].price;
							}
						}
					}
					
					counts[sectionData.id].unlocked = unlockedCount;
					counts[sectionData.id].unlockedBuyValue = unlockedBuyValue;
					counts[sectionData.id].unlockedSellValue = unlockedSellValue;
					gwuCounts[sectionData.id].unlocked = gwuUnlockedCount;
					gwuCounts[sectionData.id].unlockedBuyValue = gwuUnlockedBuyValue;
					gwuCounts[sectionData.id].unlockedSellValue = gwuUnlockedSellValue;
					
					updateCounts();
					updateGroupVisibility();	
					updateThresholdCalculation();
				}.bind(null, sectionData),
				error: function(val) {
					$('#api-error').toggleClass('hidden', false);
					clear();
					console.log('Error', val);
				}
			});		
		}
	}
}

function updateSectionFolding() {
  var sections = $(".section");
  for (var i = 0; i < sections.length; ++i) {
    if( $('#' + sections[i].id +' .item:not(.hidden):not(.unlocked)').length == 0) {
      $(sections[i]).removeAttr("open");
    } else {
      $(sections[i]).attr("open", "open");
    }
  }
}

function updateCounts() {
	updateCountsType('', counts);
	updateCountsType('gwu-', gwuCounts);
}

function updateCountsType(type, countData) {
	var unlockedCount = 0;
	var unlockedBuyValue = 0;
	var unlockedSellValue = 0;
	var totalCount = 0;
	var totalBuyValue = 0;
	var totalSellValue = 0;
	for (var section in countData) {
		if (countData.hasOwnProperty(section)) {
            $('#' + section + '-' + type + 'total').text(countData[section].total);
            $('#' + section + '-' + type + 'unlocked-count').text(countData[section].unlocked);
            $('#' + section + '-' + type + 'total-buy-gold').text(Math.floor(countData[section].totalBuyValue / 10000));
            $('#' + section + '-' + type + 'total-buy-silver').text(Math.floor(countData[section].totalBuyValue / 100) % 100);
            $('#' + section + '-' + type + 'total-buy-copper').text(countData[section].totalBuyValue % 100);
            $('#' + section + '-' + type + 'buy-unlocked-gold').text(Math.floor(countData[section].unlockedBuyValue / 10000));
            $('#' + section + '-' + type + 'buy-unlocked-silver').text(Math.floor(countData[section].unlockedBuyValue / 100) % 100);
            $('#' + section + '-' + type + 'buy-unlocked-copper').text(countData[section].unlockedBuyValue % 100);
            
            $('#' + section + '-' + type + 'total-sell-gold').text(Math.floor(countData[section].totalSellValue / 10000));
            $('#' + section + '-' + type + 'total-sell-silver').text(Math.floor(countData[section].totalSellValue / 100) % 100);
            $('#' + section + '-' + type + 'total-sell-copper').text(countData[section].totalSellValue % 100);
            $('#' + section + '-' + type + 'sell-unlocked-gold').text(Math.floor(countData[section].unlockedSellValue / 10000));
            $('#' + section + '-' + type + 'sell-unlocked-silver').text(Math.floor(countData[section].unlockedSellValue / 100) % 100);
            $('#' + section + '-' + type + 'sell-unlocked-copper').text(countData[section].unlockedSellValue % 100);
            totalCount += countData[section].total;
            totalBuyValue += countData[section].totalBuyValue;
            totalSellValue += countData[section].totalSellValue;
            unlockedCount += countData[section].unlocked;
            unlockedBuyValue += countData[section].unlockedBuyValue;
            unlockedSellValue += countData[section].unlockedSellValue;
        }
    }
	$('#' + type + 'total').text(totalCount);
	$('#' + type + 'total-unlocked').text(unlockedCount);
	$('#' + type + 'total-buy-gold').text(Math.floor(totalBuyValue / 10000));
	$('#' + type + 'total-buy-silver').text(Math.floor(totalBuyValue / 100) % 100);
	$('#' + type + 'total-buy-copper').text(totalBuyValue % 100);
	$('#' + type + 'total-buy-unlocked-gold').text(Math.floor(unlockedBuyValue / 10000));
	$('#' + type + 'total-buy-unlocked-silver').text(Math.floor(unlockedBuyValue / 100) % 100);
	$('#' + type + 'total-buy-unlocked-copper').text(unlockedBuyValue % 100);
	$('#' + type + 'total-sell-gold').text(Math.floor(totalSellValue / 10000));
	$('#' + type + 'total-sell-silver').text(Math.floor(totalSellValue / 100) % 100);
	$('#' + type + 'total-sell-copper').text(totalSellValue % 100);
	$('#' + type + 'total-sell-unlocked-gold').text(Math.floor(unlockedSellValue / 10000));
	$('#' + type + 'total-sell-unlocked-silver').text(Math.floor(unlockedSellValue / 100) % 100);
	$('#' + type + 'total-sell-unlocked-copper').text(unlockedSellValue % 100);
}

function buildTPSorted(sectionId, tpItems, priceLookup, priceFunc, prefix) {
	var itemPriceLookup = {};		
	priceLookup[sectionId] = itemPriceLookup;
	
	for (var i = 0; i < tpItems.length; ++i) {
		var itemValue = priceFunc(tpItems[i]);
		if (!isNaN(itemValue)) {
			itemPriceLookup[tpItems[i].id] = {'name' : tpItems[i].name, 'price' : itemValue, 'gwu' : tpItems[i].sources.includes('gwu'), 'details' : tpItems[i], 'section' : sectionId};
		} else {
			itemPriceLookup[tpItems[i].id] = {'name' : tpItems[i].name, 'price' : 0, 'gwu' : tpItems[i].sources.includes('gwu'), 'details' : tpItems[i], 'section' : sectionId};
		}
	}
	
	tpItems.sort(function (a, b) {
		var valA = priceFunc(a);
		var valB = priceFunc(b);
		if (isNaN(valA)) {
			if (isNaN(valB)) {
				return 0;
			} else {
				return 1;
			}
		} else if (isNaN(valB)) {
			return -1;
		}
		return valA - valB;
	});
	
	
	if (tpItems.length > 0) {
		var items = populateContent(tpItems, sectionId + '-' + prefix + '-tp');
		var section = '';
		section += '<div class="section-body">';
		section += items;
		section += '</div>';
		return section;
	}
	return '';
}

function buildSections() {
	var root = $('#sectioned-content');
	for (var sectionIndex = 0; sectionIndex < metadata.items.length; ++sectionIndex) {
		var sectionData = metadata.items[sectionIndex];
		populateGroupNames(sectionData);
		buildSection(root, sectionData);
	}	
}

function populateGroupNames(sectionData) {
	for (var groupIndex = 0; groupIndex < sectionData.groups.length; ++groupIndex) {
		var groupName = sectionData.groups[groupIndex].groupName;
		for (var itemIndex = 0; itemIndex < sectionData.groups[groupIndex].content.length; ++itemIndex) {
			sectionData.groups[groupIndex].content[itemIndex].groupName = groupName;
		}
	}
}

function calculateTotalValue(tpItems, priceFunc) {
	var totalValue = 0;
	for (var i = 0; i < tpItems.length; ++i) {
		var itemValue = priceFunc(tpItems[i]);
		if (!isNaN(itemValue)) {
			totalValue += itemValue;
		}
	}
	return totalValue;
}


function getSellPrice(item) {
	var itemValue = NaN;
	if (item.priceData != null) {
		itemValue = item.priceData.bestSellPrice.price;
		if (isNaN(itemValue)) {
			itemValue = item.priceData.bestBuyPrice.price;
		}
	}
	var vendorPrice = getVendorPrice(item);
	if (isNaN(itemValue) || (!isNaN(vendorPrice) && vendorPrice < itemValue)) {
		itemValue = vendorPrice;
	}
	return itemValue;
}

function getBuyPrice(item) {
	var itemValue = NaN;
	if (item.priceData != null) {
		itemValue = item.priceData.bestBuyPrice.price;
		if (isNaN(itemValue)) {
			itemValue = item.priceData.bestSellPrice.price;
		}
	}
	var vendorPrice = getVendorPrice(item);
	if (isNaN(itemValue) || (!isNaN(vendorPrice) && vendorPrice < itemValue)) {
		itemValue = vendorPrice;
	}
	return itemValue;
}

function getVendorPrice(item) {
	if (item.vendors == null) {
		return NaN;
	}
	var vendorPrice = NaN;
	for (var i = 0; i < item.vendors.length; ++i) {
		var vendor = item.vendors[i]
		if (vendor.cost.length == 1 && vendor.cost[0].type == 'gold' && (isNaN(vendorPrice) || vendorPrice > vendor.cost[0].value)) {
			vendorPrice = vendor.cost[0].value;
		}
	}
	return vendorPrice;
}
	
function buildSection(root, sectionData) {
	var section = '<details id="' + sectionData.id + '-section" class="section" open="true"><summary class="section-header">' + sectionData.name + '</summary>';
	var count = 0;
	var gwuCount = 0;
	for (var groupIndex = 0; groupIndex < sectionData.groups.length; ++groupIndex) {
		count += sectionData.groups[groupIndex].content.length;
		gwuCount += sectionData.groups[groupIndex].content.filter(gwuFilter).length;
	}
	var countData = {};
	countData.total = count;
	countData.unlocked = 0;
	counts[sectionData.id] = countData;
	var gwuCountData = {};
	gwuCountData.total = gwuCount;
	gwuCountData.unlocked = 0;
	gwuCounts[sectionData.id] = gwuCountData;
	
	var tpItems = extractTPItems(sectionData);
	
	var groups = buildSectionGroups(sectionData);
	var sellSection = buildTPSorted(sectionData.id, tpItems, sellPriceLookup, getSellPrice, 'sell');
	var buySection = buildTPSorted(sectionData.id, tpItems, buyPriceLookup, getBuyPrice, 'buy');
	
	countData.totalBuyValue = calculateTotalValue(tpItems, getBuyPrice);
	countData.totalSellValue = calculateTotalValue(tpItems, getSellPrice);
	countData.unlockedBuyValue = 0;
	countData.unlockedSellValue = 0;
	
	gwuCountData.totalBuyValue = calculateTotalValue(tpItems.filter(gwuFilter), getBuyPrice);
	gwuCountData.totalSellValue = calculateTotalValue(tpItems.filter(gwuFilter), getSellPrice);
	gwuCountData.unlockedBuyValue = 0;
	gwuCountData.unlockedSellValue = 0;
	
	if (!sectionData.unlockUrl) {		
	  section += '<p>Note: Account unlock information not available for this section</p>';
	} else {
	  section += '<div class="section-counts">';
	  section += '<p id="' + sectionData.id + '-count">Unlocked: <span id="' + sectionData.id + '-unlocked-count">0</span> / <span id="' + sectionData.id + '-total-count">' +  count + '</span></p>';
	  section += '<p>Unlocked by buy value: <span id="' + sectionData.id + '-buy-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-buy-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-buy-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of '
	  section += '<span id="' + sectionData.id + '-buy-total-gold">' + Math.floor(countData.totalBuyValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
	  section += '<span id="' + sectionData.id + '-buy-total-silver">' + (Math.floor(countData.totalBuyValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
	  section += '<span id="' + sectionData.id + '-buy-total-copper">' + (countData.totalBuyValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></p>';
	  
	  section += '<p>Unlocked by sell value: <span id="' + sectionData.id + '-sell-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-sell-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-sell-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of ';
	  section += '<span id="' + sectionData.id + '-sell-total-gold">' + Math.floor(countData.totalSellValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
	  section += '<span id="' + sectionData.id + '-sell-total-silver">' + (Math.floor(countData.totalSellValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
	  section += '<span id="' + sectionData.id + '-sell-total-copper">' + (countData.totalSellValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></p>';
	  section += '</div>';
	  
	  section += '<div class="gwu-section-counts hidden">';
	  section += '<p id="' + sectionData.id + '-gwu-count">Unlocked: <span id="' + sectionData.id + '-gwu-unlocked-count">0</span> / <span id="' + sectionData.id + '-gwu-total-count">' +  gwuCount + '</span></p>';
	  section += '<p>Unlocked by buy value: <span id="' + sectionData.id + '-gwu-buy-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-gwu-buy-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-gwu-buy-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of '
	  section += '<span id="' + sectionData.id + '-gwu-buy-total-gold">' + Math.floor(gwuCountData.totalBuyValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
	  section += '<span id="' + sectionData.id + '-gwu-buy-total-silver">' + (Math.floor(gwuCountData.totalBuyValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
	  section += '<span id="' + sectionData.id + '-gwu-buy-total-copper">' + (gwuCountData.totalBuyValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></p>';
	  
	  section += '<p>Unlocked by sell value: <span id="' + sectionData.id + '-gwu-sell-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-gwu-sell-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-gwu-sell-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of ';
	  section += '<span id="' + sectionData.id + '-gwu-sell-total-gold">' + Math.floor(gwuCountData.totalSellValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
	  section += '<span id="' + sectionData.id + '-gwu-sell-total-silver">' + (Math.floor(gwuCountData.totalSellValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
	  section += '<span id="' + sectionData.id + '-gwu-sell-total-copper">' + (gwuCountData.totalSellValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span>';
	  section += '</div>';
	}
	section += '<div class="section-groups">';
	section += groups;
	section += '</div>'
	if (tpItems.length > 0) {
		section += '<div class="tp-buy-sorted hidden">';
		section += buySection;
		section += '</div>'
		section += '<div class="tp-sell-sorted hidden">';
		section += sellSection;
		section += '</div>'
	}
	section += '</details>';
	root.append(section);
	
	if (sectionData.groups.length > 1) {
		for (var groupIndex = 0; groupIndex < sectionData.groups.length; ++groupIndex) {
			var groupData = sectionData.groups[groupIndex];
			setupClickHandlers(sectionData.id, groupData.content)									
		}
	} else {
		setupClickHandlers(sectionData.id, sectionData.groups[0].content)
	}
	setupClickHandlers(sectionData.id + '-buy-tp', tpItems);
	setupClickHandlers(sectionData.id + '-sell-tp', tpItems);
}

function buildSectionGroups(sectionData) {
	var groups = '';
	if (sectionData.groups.length > 1) {
		for (var groupIndex = 0; groupIndex < sectionData.groups.length; ++groupIndex) {
			var groupData = sectionData.groups[groupIndex];
			var buyTotal = calculateTotalValue(groupData.content, getBuyPrice);
			var sellTotal = calculateTotalValue(groupData.content, getSellPrice);

			var group = '<div class="group" data-buy-total="' + buyTotal + '" data-sell-total="' + sellTotal + '" data-ordering = "' + groupIndex + '"><h3>' + groupData.groupName + '</h3>';
			group += '<div class="section-body">';
			group += populateContent(groupData.content, sectionData.id);
			group += '</div></div>';
			groups += group;
		}
	} else {
		var contentSection = '<div class="section-body">';
		contentSection += populateContent(sectionData.groups[0].content, sectionData.id);
		contentSection += '</div>';
		groups += contentSection;
	}
	return groups;
}

function extractTPItems(sectionData) {
	var tpItems = [];	
	for (var groupIndex = 0; groupIndex < sectionData.groups.length; ++groupIndex) {
		var groupData = sectionData.groups[groupIndex];
		for (var itemIndex = 0; itemIndex < groupData.content.length; ++itemIndex) {
			if (groupData.content[itemIndex].priceData || !isNaN(getVendorPrice(groupData.content[itemIndex]))) {
				tpItems.push(groupData.content[itemIndex]);
			}
		}
	}
	return tpItems;
}

function updateGroupVisibility() {
	var groups = $('.group');
	for (var i = 0; i < groups.length; ++i) {
		$(groups[i]).toggle($(groups[i]).find('.item').not('.unlocked').not('.hidden').length > 0);
	}
}

function clear() {
	$('.item').toggleClass('unlocked', false);
	for (var section in counts) {
		if (counts.hasOwnProperty(section)) {
			counts[section].unlocked = 0;
			counts[section].unlockedValue = 0;
	    }
	}
	for (var section in gwuCounts) {
		if (gwuCounts.hasOwnProperty(section)) {
			gwuCounts[section].unlocked = 0;
			gwuCounts[section].unlockedValue = 0;
	    }
	}
	updateCounts();
	updateGroupVisibility();
	updateThresholdCalculation();
}

function displayItem(itemData, id) {
	var result = '<div id="' + id + '" class="item';
	if (itemData.rarity) {
		result += ' ' + itemData.rarity;
	}
	for (var i = 0; i < itemData.sources.length; ++i) {
		var source = itemData.sources[i];
		result += ' ' + source;
	}
	if ($.inArray('winterberries', itemData.sources) != -1 || $.inArray('unboundmagic', itemData.sources) != -1 || $.inArray('petrifiedwood', itemData.sources) != -1 || $.inArray('fireorchidblossom', itemData.sources) != -1 || $.inArray('orrianpearl', itemData.sources) != -1 || $.inArray('jadeshard', itemData.sources) != -1) {
		result += ' ls3';
	}
	if ($.inArray('volatilemagic', itemData.sources) != -1 || $.inArray('kralkatiteore', itemData.sources) != -1 || $.inArray('difluorite', itemData.sources) != -1 || $.inArray('swimspeedinfusion', itemData.sources) != -1 || $.inArray('mistonium', itemData.sources) != -1 || $.inArray('inscribedshard', itemData.sources) != -1) {
		result += ' ls4';
	}
  if ($.inArray('hatchedchili', itemData.sources) != -1 || $.inArray('eternaliceshard', itemData.sources) != -1 || $.inArray('eitriteingot', itemData.sources) != -1) {
		result += ' ibs';
	}
	if (itemData.image) {
		result += ' icon';
	} else {
		result += ' color';
	}
	result += '" style="';
	if (itemData.image) {
		result += " background-image: url('./img/" + imageMap[itemData.image] + "'); background-position: -" + itemData.xOffset + 'px -' + itemData.yOffset + 'px;';
	}
	result += '" title="' + itemData.name + '">';
	return result;
}

function addSources(itemData) {
	var result = '';
    if ($.inArray("gold", itemData.sources) != -1 && $.inArray("blt", itemData.sources) == -1) {
		result += '<span class="base-icon gold-icon" role="img" aria-label="Gold"></span>';
	}
    for (method of acquisitionMethods) {
        if ($.inArray(method.id, itemData.sources) != -1 && !method.hideOnIcon) {
            result += '<span class="source-icon base-icon ' + method.id + '-icon" role="img" aria-label="' + method.name + '"></span>';
        }
    }
	return result;
}

function populateContent(content, typeId) {
	var result = '';
	for (var i = 0; i < content.length; ++i) {
		var itemData = content[i];		
		var item = displayItem(itemData, typeId + '-' + itemData.id);
		if (itemData.color) {
			item += '<div class="swatch" style="background-color: ' + itemData.color + ';"></div>';
		}
		item += '<div class="name">' + itemData.name + '</div>';
		item += '<div class="sources">';
		item += addSources(itemData);
		item += '</div>';
		item += '</div>';
		result += item;
	}
	return result;
}

function setupClickHandlers(typeId, content) {
	for (var i = 0; i < content.length; ++i) {
		var itemData = content[i];		
		var selection = $('#' + typeId + '-' + itemData.id);
		selection.click(function(item) {
			showDetails(item, '');
		}.bind(null, itemData));
	}
}

function showDetails(item, prefix) {
	var selectionIcon = displayItem(item, prefix + 'selection-icon');
	if (item.color) {
		selectionIcon += '<div id="' + prefix + 'selection-swatch" class="swatch" style="background-color: ' + item.color + ';"></div>';
	}
	selectionIcon += '</div>';
	$('#' + prefix + 'selection-icon').replaceWith(selectionIcon);
	$('#' + prefix + 'selection-name').children('span').text(item.name);
	$('#' + prefix + 'selection-group').children('span').text(item.groupName);
	if (item.rarity) {
		$('#' + prefix + 'selection-rarity').children('span').text(item.rarity);
	} else {
		$('#' + prefix + 'selection-rarity').children('span').text('Unknown');
	}
	if (item.chatcode != null) {
		$('#' + prefix + 'selection-wiki-link').children('a').attr('href', 'https://wiki.guildwars2.com/index.php?search=' + encodeURIComponent(item.chatcode));
	} else {
		$('#' + prefix + 'selection-wiki-link').children('a').attr('href', 'https://wiki.guildwars2.com/index.php?search=' + encodeURIComponent(item.name));
	}
	$('#' + prefix + 'selection-chat-code').toggle(item.chatcode != null);
	$('#' + prefix + 'selection-chat-code').children('span').text(item.chatcode);
	$('#' + prefix + 'selection-id').children('span').text(item.id);
	
	for (method of acquisitionMethods) {
		$('#' + prefix + 'acquisition-' + method.id).toggle($.inArray(method.id, item.sources) != -1);   
	}
	$('#' + prefix + 'selection-tp-info').toggle($.inArray("tp", item.sources) != -1);
	if (item.vendors && item.vendors.length > 0) {
		$('#' + prefix + 'selection-vendors').toggle(true);
	} else {
		$('#' + prefix + 'selection-vendors').toggle(false);
	}
	var vendorList = $('#' + prefix + 'selection-vendor-list');
	vendorList.empty();
	if (item.vendors) {
		for (var i = 0; i < item.vendors.length; ++i) {
			var entry = '<div class="vendor"><a href="' + item.vendors[i].vendorUrl + '" target="_blank">' + item.vendors[i].vendorName + '</a>:';
			for (var j = 0; j < item.vendors[i].cost.length; ++j) {
				if (j > 0) {
					entry += ' +'
				}
				if (item.vendors[i].cost[j].type == "gold") {
					var gold = Math.floor(item.vendors[i].cost[j].value / 10000);
					var silver = Math.floor(item.vendors[i].cost[j].value / 100) % 100;
					var copper = Math.floor(item.vendors[i].cost[j].value % 100);
					if (gold > 0) {
						entry += ' ' + gold + '<span class="base-icon gold-icon" role="img" aria-label="Gold"></span>'
					}
					entry += ' ' + silver + '<span class="base-icon silver-icon" role="img" aria-label="Silver"></span>';
					entry += ' ' + copper + '<span class="base-icon copper-icon" role="img" aria-label="Copper"></span>';
				} else if (acquisitionMethodsLookup[item.vendors[i].cost[j].type] != null) {
					method = acquisitionMethodsLookup[item.vendors[i].cost[j].type]
					entry += ' ' + item.vendors[i].cost[j].value + '<span class="base-icon ' + method.id + '-icon" role="img" aria-label="' + method.name + '"></span>'; 
				} else if (item.vendors[i].cost[j].type == "karma") {
					entry += ' ' + item.vendors[i].cost[j].value + '<span class="base-icon karma-icon" role="img" aria-label="Karma"></span>'; 
				} else {
					entry += ' ' + item.vendors[i].cost[j].value + ' ' + item.vendors[i].cost[j].type; 
				}
			}
			entry += '</div>';
			vendorList.append(entry);
		}
	}
	
	if (item.priceData) {
		if (item.priceData.bestBuyPrice.price) {
		  if(item.priceData.bestBuyPrice.itemName != item.name) {
		    $('#' + prefix + 'selection-buy-name').text("(" + item.priceData.bestBuyPrice.itemName + ")");
		  } else {
		    $('#' + prefix + 'selection-buy-name').text("");
		  }
			$('#' + prefix + 'selection-buy-gold').text(Math.floor(item.priceData.bestBuyPrice.price / 10000));
			$('#' + prefix + 'selection-buy-silver').text(Math.floor(item.priceData.bestBuyPrice.price / 100) % 100);
			$('#' + prefix + 'selection-buy-copper').text(item.priceData.bestBuyPrice.price % 100);
		} else {
			$('#' + prefix + 'selection-buy-gold').text('0');
			$('#' + prefix + 'selection-buy-silver').text('0');
			$('#' + prefix + 'selection-buy-copper').text('0');
		}
		if (item.priceData.bestSellPrice.price) {
		   if(item.priceData.bestSellPrice.itemName != item.name) {
		     $('#' + prefix + 'selection-sell-name').text("(" + item.priceData.bestSellPrice.itemName + ")");
       } else {
      	 $('#' + prefix + 'selection-sell-name').text("");
       }
			$('#' + prefix + 'selection-sell-gold').text(Math.floor(item.priceData.bestSellPrice.price / 10000));
			$('#' + prefix + 'selection-sell-silver').text(Math.floor(item.priceData.bestSellPrice.price / 100) % 100);
			$('#' + prefix + 'selection-sell-copper').text(item.priceData.bestSellPrice.price % 100);
		} else {
			$('#' + prefix + 'selection-sell-gold').text('-');
			$('#' + prefix + 'selection-sell-silver').text('-');
			$('#' + prefix + 'selection-sell-copper').text('-');
		}
	}
}
