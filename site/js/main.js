jQuery.fn.exists = function(){return this.length>0;}

var metadata;
var lookup = {};
var buyPriceLookup = {};
var sellPriceLookup = {};
var counts = {};
var gwuCounts = {};
var bountyCounts = {};
var unlocks = {};
var imageMap = {};
var acquisitionMethods = [

    { id: "tp", name: "Trading Post", category: "General", hideOnIcon: true},
    { id: "vendor", name: "Vendor", category: "General", hideOnIcon: true},
    { id: "craft", name: "Crafting", category: "General"},
    { id: "achievement", name: "Achievement", category: "General"},
    { id: "achievementpoint", name: "Achievement Point", category: "General"},
    { id: "hallofmonuments", name: "Hall of Monuments", category: "General"},
    { id: "story", name: "Story Reward", category: "General"},
    { id: "event", name: "Event Reward", category: "General"},
    { id: "adventure", name: "Adventure Reward", category: "General"},
    { id: "quest", name: "Quest Reward", category: "General"},
    { id: "linked", name: "Linked", category: "General"},

    { id: "birthday1", name: "First Birthday Gift", category: "Anniversary"},
    //{ id: "birthday2", name: "Second Birthday Gift", category: "Anniversary"},
    { id: "birthday3", name: "Third Birthday Gift", category: "Anniversary"},
    { id: "birthday4", name: "Fourth Birthday Gift", category: "Anniversary"},
    { id: "birthday5", name: "Fifth Birthday Gift", category: "Anniversary"},
    { id: "birthday6", name: "Sixth Birthday Gift", category: "Anniversary"},
    { id: "birthday7", name: "Seventh Birthday Gift", category: "Anniversary"},
    { id: "birthday8", name: "Eighth Birthday Gift", category: "Anniversary"},
    { id: "birthday9", name: "Ninth Birthday Gift", category: "Anniversary"},
    { id: "birthday10", name: "Tenth+ Birthday Gift", category: "Anniversary"},

    { id: "gold", name: "Coins", category: "Common", hideOnIcon: true},
    { id: "karma", name: "Karma", category: "Common"},
    { id: "astralacclaim", name: "Astral Acclaim", category: "Common"},
    { id: "laurel", name: "Laurel", category: "Common"},
    { id: "spiritshard", name: "Spirit Shard", category: "Common"},
    { id: "globofectoplasm", name: "Glob of Ectoplasm", category: "Common"},
    { id: "guildcommendation", name: "Guild Commendation", category: "Common"},
    { id: "provisionertoken", name: "Provisioner Token", category: "Common"},

    { id: "grandmasterarmorsmithsmark", name: "Grandmaster Armorsmith's Mark", category: "Crafting Component"},
    { id: "grandmasterleatherworkersmark", name: "Grandmaster Leatherworker's Mark", category: "Crafting Component"},
    { id: "grandmastertailorsmark", name: "Grandmaster Tailor's Mark", category: "Crafting Component"},
    { id: "grandmasterartifactmark", name: "Grandmaster Artificer's Mark", category: "Crafting Component"},
    { id: "grandmasterhuntsmanmark", name: "Grandmaster Huntsman's Mark", category: "Crafting Component"},
    { id: "grandmasterweaponmark", name: "Grandmaster Weaponsmith's Mark", category: "Crafting Component"},
    { id: "spoolofsilkthread", name: "Spool of Silk Thread", category: "Crafting Component"},
    { id: "spiritwoodplank", name: "Spiritwood Plank", category: "Crafting Component"},
    { id: "mithrilband", name: "Mithril Band", category: "Crafting Component"},
    { id: "giftofbones", name: "Gift of Bones", category: "Crafting Component"},
    { id: "giftoffangs", name: "Gift of Fangs", category: "Crafting Component"},
    { id: "giftofdust", name: "Gift of Dust", category: "Crafting Component"},
    { id: "giftofvenom", name: "Gift of Venom", category: "Crafting Component"},
    { id: "giftofscales", name: "Gift of Scales", category: "Crafting Component"},
    { id: "giftofclaws", name: "Gift of Claws", category: "Crafting Component"},
    { id: "giftoftotems", name: "Gift of Totems", category: "Crafting Component"},
    { id: "giftofblood", name: "Gift of Blood", category: "Crafting Component"},
    { id: "cherriesinbulk", name: "Cherries In Bulk", category: "Crafting Component"},
    { id: "pinenutsinbulk", name: "Pinenuts In Bulk", category: "Crafting Component"},
    { id: "almondsinbulk", name: "Almonds In Bulk", category: "Crafting Component"},
    { id: "peachesinbulk", name: "Peaches In Bulk", category: "Crafting Component"},
    { id: "pouchofblackpigment", name: "Pouch of Black Pigment", category: "Crafting Component"},

    { id: "taleofdungeondelving", name: "Tales of Dungeon Delving", category: "Core / Season 1 / Season 2"},
    { id: "karkashell", name: "Karka Shell", category: "Core / Season 1 / Season 2"},
    { id: "foundheirloom", name: "Found Heirloom", category: "Core / Season 1 / Season 2"},
    { id: "banditcrest", name: "Bandit Crest", category: "Core / Season 1 / Season 2"},
    { id: "swimspeedinfusion", name: "Swim-Speed Infusion +10", category: "Core / Season 1 / Season 2"},
    { id: "racingmedallion", name: "Racing Medallion", category: "Core / Season 1 / Season 2"},
    { id: "proofoflegend", name: "Proof of Legend", category: "Core / Season 1 / Season 2"},

    { id: "airshippart", name: "Airship Part", category: "Heart of Thorns"},
    { id: "aurillium", name: "Lump of Aurillium", category: "Heart of Thorns"},
    { id: "leylinecrystal", name: "Ley Line Crystal", category: "Heart of Thorns"},
    { id: "reclaimedplate", name: "Reclaimed Metal Plate", category: "Heart of Thorns"},
    { id: "chakegg", name: "Chak Egg", category: "Heart of Thorns"},
    { id: "crystallineore", name: "Crystalline Ore", category: "Heart of Thorns"},

    { id: "unboundmagic", name: "Unbound Magic", category: "Living World Season 3"},
    { id: "bloodruby", name: "Blood Ruby", category: "Living World Season 3"},
    { id: "petrifiedwood", name: "Petrified Wood", category: "Living World Season 3"},
    { id: "circusvoucher", name: "Circus Voucher", category: "Living World Season 3"},
    { id: "winterberries", name: "Fresh Winterberry", category: "Living World Season 3"},
    { id: "jadeshard", name: "Jade Shard", category: "Living World Season 3"},
    { id: "fireorchidblossom", name: "Fire Orchid Blossom", category: "Living World Season 3"},
    { id: "orrianpearl", name: "Orrian Pearl", category: "Living World Season 3"},

    { id: "tradecontract", name: "Trade Contract", category: "Path of Fire"},
    { id: "elegymosaic", name: "Elegy Mosaic", category: "Path of Fire"},
    { id: "casinocoin", name: "Casino Coin", category: "Path of Fire"},
    { id: "orderofshadowsseal", name: "Order of Shadows Seal", category: "Path of Fire"},

    { id: "volatilemagic", name: "Volatile Magic", category: "Living World Season 4"},
    { id: "kralkatiteore", name: "Kralkatite Ore", category: "Living World Season 4"},
    { id: "difluorite", name: "Difluorite Crystal", category: "Living World Season 4"},
    { id: "inscribedshard", name: "Inscribed Shard", category: "Living World Season 4"},
    { id: "mistonium", name: "Lump of Mistonium", category: "Living World Season 4"},
    { id: "brandedmass", name: "Branded Mass", category: "Living World Season 4"},
    { id: "mistbornmote", name: "Mistborn Mote", category: "Living World Season 4"},

    { id: "hatchedchili", name: "Hatched Chili", category: "Living World Season 5"},
    { id: "eternaliceshard", name: "Eternal Ice Shard", category: "Living World Season 5"},
    { id: "eitriteingot", name: "Eitrite Ingot", category: "Living World Season 5"},
    { id: "ancientdeldrimorgear", name: "Ancient Deldrimor Gear", category: "Living World Season 5"},
    { id: "essentialoilofshadows", name: "Essential Oil of Shadows", category: "Living World Season 5"},
    { id: "icepearl", name: "Ice Pearl", category: "Living World Season 5"},
    { id: "blessedravenstatuette", name: "Blessed Raven Statuette", category: "Living World Season 5"},
    { id: "frozensparkoflife", name: "Frozen Spark of Life", category: "Living World Season 5"},
    { id: "pristinefangofthewhisper", name: "Pristine Fang of the Whisper", category: "Living World Season 5"},
    { id: "tyriandefenseseal", name: "Tyrian Defense Seal", category: "Living World Season 5"},

    { id: "imperialfavor", name: "Imperial Favor", category: "End of Dragons"},
    { id: "researchnote", name: "Research Note", category: "End of Dragons"},
    { id: "canachcoin", name: "Canach Coin", category: "End of Dragons"},
    { id: "ancientcoin", name: "Ancient Coin", category: "End of Dragons"},
    { id: "writofseitungprovince", name: "Writ of Seitung Province", category: "End of Dragons"},
    { id: "writofnewkainengcity", name: "Writ of New Kaineng City", category: "End of Dragons"},
    { id: "writofechovaldwilds", name: "Writ of Echovald Wilds", category: "End of Dragons"},
    { id: "writofdragonsend", name: "Writ of Dragon's End", category: "End of Dragons"},
    { id: "pieceofcrustaceanmeat", name: "Piece of Crustacean Meat", category: "End of Dragons"},
    { id: "flawlessfishfillet", name: "Flawless Fish Fillet", category: "End of Dragons"},

    { id: "unusualcoin", name: "Unusual Coin", category: "Secrets of the Obscure"},
    { id: "greenprophetshard", name: "Green Prophet Shard", category: "Secrets of the Obscure"},
    { id: "caseofcapturedlightning", name: "Case of Captured Lightning", category: "Secrets of the Obscure"},
    { id: "pouchofstardust", name: "Pouch of Stardust", category: "Secrets of the Obscure"},
    { id: "purifiedkryptisessence", name: "Purified Kryptis Essence", category: "Secrets of the Obscure"},
    { id: "amalgamatedkryptisessence", name: "Amalgamated Kryptis Essence", category: "Secrets of the Obscure"},
    { id: "clotofcongealedscreams", name: "Clot of Congealed Screams", category: "Secrets of the Obscure"},

    { id: "fragmentofprismaticpersuasion", name: "Fragment of Prismatic Persuasion", category: "Facet of Aurene"},
    { id: "fragmentofprismaticfury", name: "Fragment of Prismatic Fury", category: "Facet of Aurene"},
    { id: "fragmentofprismaticplant", name: "Fragment of Prismatic Plant", category: "Facet of Aurene"},
    { id: "fragmentofprismaticfire", name: "Fragment of Prismatic Fire", category: "Facet of Aurene"},
    { id: "fragmentofprismaticlife", name: "Fragment of Prismatic Life", category: "Facet of Aurene"},
    { id: "fragmentofprismaticshadows", name: "Fragment of Prismatic Shadows", category: "Facet of Aurene"},

    { id: "fractalrelic", name: "Fractal Relic", category: "Cooperative"},
    { id: "pristinefractalrelic", name: "Pristine Fractal Relic", category: "Cooperative"},
    { id: "fractalresearchpage", name: "Fractal Research Page", category: "Cooperative"},
    { id: "fractaljournal", name: "Fractal Journal", category: "Cooperative"},
    { id: "goldenfractalrelic", name: "Golden Fractal Relic", category: "Cooperative"},
    { id: "integratedmatrix", name: "Integrated Fractal Matrix", category: "Cooperative"},
    { id: "unstablefractalessence", name: "Unstable Fractal Essence", category: "Cooperative"},
    { id: "magnetiteshard", name: "Magnetite Shard", category: "Cooperative"},
    { id: "blueprophetshard", name: "Blue Prophet Shard", category: "Cooperative"},

    { id: "pvp", name: "Reward Track", category: "Competitive"},
    { id: "pvpleagueticket", name: "PvP League Ticket", category: "Competitive"},
    { id: "shardofglory", name: "Shards of Glory", category: "Competitive"},
    { id: "ascendedshardofglory", name: "Ascended Shards of Glory", category: "Competitive"},
    //{ id: "pvpleague", name: "PvP League Leaderboard", category: "Competitive"},
    { id: "pvptournament", name: "Tournaments & Special Events", category: "Competitive"},
    { id: "pvptournamentvoucher", name: "PvP Tournament Voucher", category: "Competitive"},
    { id: "boh", name: "Badge of Honor", category: "Competitive"},
    { id: "wvwsct", name: "WvW Skirmish Claim Ticket", category: "Competitive"},
    { id: "memoryofbattle", name: "Memory of Battle", category: "Competitive"},
    { id: "potionofascalonianmages", name: "Potion of Ascalonian Mages", category: "Competitive"},

    { id: "exoticluck", name: "Essence of Luck (Exotic)", category: "Festival"},
    { id: "legendaryluck", name: "Essence of Luck (Legendary)", category: "Festival"},
    { id: "bauble", name: "Bauble Bubbles", category: "Festival"},
    { id: "crimsonassassintoken", name: "Crimson Assassin Token", category: "Festival"},
    { id: "jorbreaker", name: "Jorbreaker", category: "Festival"},
    { id: "festivaltoken", name: "Festival Token", category: "Festival"},
    { id: "favorofthefestival", name: "Favor of the Festival", category: "Festival"},
    { id: "bundleofloot", name: "Bundle of Loot", category: "Festival"},
    { id: "candycorncob", name: "Candy Corn Cob", category: "Festival"},
    { id: "snowflake", name: "Snowflake", category: "Festival"},
    { id: "snowdiamond", name: "Snow Diamond", category: "Festival"},
    { id: "enchantedsnowball", name: "Enchanted Snowball", category: "Festival"},

    { id: "gem", name: "Gems", category: "Gem Store"},
    { id: "deluxe", name: "Purchase Bonus", category: "Gem Store"},
    { id: "bls", name: "Black Lion Statuette", category: "Gem Store"},
    { id: "blt", name: "Black Lion Claim Ticket", category: "Gem Store"},
    { id: "blmt", name: "Black Lion Miniature Claim Ticket", category: "Gem Store"},
    { id: "blsprocket", name: "Black Lion Commemorative Sprocket", category: "Gem Store"},
    { id: "gwu", name: "Guaranteed Wardrobe Unlock", category: "Gem Store", hideOnIcon: true},
    { id: "bounty", name: "Knife Tail Gang Hunting Bond", category: "Gem Store", hideOnIcon: true},
    { id: "promotionalevent", name: "Promotional Event", category: "Gem Store"},

    { id: "mountlicense01", name: "Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense02", name: "Istani Isles Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense03", name: "Desert Racer Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense04", name: "Distant Lands Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense05", name: "Exotic Breeds Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense06", name: "New Horizons Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense07", name: "Mistborn Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense08", name: "Curious Creatures Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense09", name: "Bizarre Beasts Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense10", name: "Lost Era Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense11", name: "Deep Wilds Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense12", name: "Canthan Menagerie Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense13", name: "Imperial Wonders Mount License", category: "Gem Store - Mount License"},
    { id: "mountlicense14", name: "Eternal Crossings Mount License", category: "Gem Store - Mount License"},
    //{ id: "mountlicense15", name: "Kaineng Lights Mount License", category: "Gem Store - Mount License"},

    { id: "eod", name: "End of Dragons", category: "Highlights", hideOnIcon: true, hideOnDetails: true},
    { id: "soto", name: "Secrets of the Obscure", category: "Highlights", hideOnIcon: true, hideOnDetails: true}

];

// Core UI Setup

function setupMenuItems() {
    $('.nav li').click(function() {
        $('.page').toggleClass('hidden', true);
        $('.nav a').toggleClass('active', false);
        var link = $(this).find('a');
        link.toggleClass('active', true);
        $(link.attr('href')).toggleClass('hidden', false);
    });
}

function loadTheme() {
  if (storageAvailable('localStorage')) {
    if (localStorage.getItem('theme')) {
      let theme = localStorage.getItem('theme');
      $('#rendering-theme-selection')[0].value = theme;
      if (theme == 'light') {
        $('#rendering-theme').attr('href', 'css/light-theme.css');
      } else {
        $('#rendering-theme').attr('href', 'css/dark-theme.css');
      }
    }
  }
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

function buildSite(data) {
    $('.lds-container').remove()

    metadata = data;
    for (o of metadata.images) {
        imageMap[o.name] = o.image;
    }
    buildLookup();
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
        updateFilter(this.value, $('#filter-by-container')[0].value);
    });

    $('#filter-by-container')[0].value = 'all';
    $('#filter-by-container').change(function() {
        updateFilter($('#filter-by-acquisition')[0].value, this.value);
    });

    $('#rendering-mode-selection')[0].value = 'icon';
    $('#rendering-mode-selection').change(function() {
        if (this.value == 'icon') {
            $('#rendering-mode').attr('href', 'css/icon-mode.css');
        } else if (this.value == 'text'){
            $('#rendering-mode').attr('href', 'css/text-mode.css');
        }
    });

    $('#rendering-theme-selection').change(function() {
        if (this.value == 'light') {
          $('#rendering-theme').attr('href', 'css/light-theme.css');
        } else {
          $('#rendering-theme').attr('href', 'css/dark-theme.css');
        }
        localStorage.setItem('theme', this.value);
    });

    if (storageAvailable('localStorage')) {
        if (localStorage.getItem('key')) {
            var key = localStorage.getItem('key');
            $('#api-key').val(key);
            filterWithApiKey(key);
        }
        var filter = 'all';
        var container = 'all'
        if (localStorage.getItem('filter')) {
            filter = localStorage.getItem('filter');
        }
        if (localStorage.getItem('container')) {
            container = localStorage.getItem('container');
        } else if (localStorage.getItem('gwu-only')) {
            container = (localStorage.getItem('gwu-only') == 'true') ? 'gwu' : 'all'
        }
        $('#filter-by-acquisition')[0].value = filter;
        $('#filter-by-container')[0].value = container;
        updateFilter(filter, container)
    }
}

function buildLookup() {
    for (type of metadata.items) {
        var typeId = type.id;
        lookup[typeId] = {};
        for (category of type.categories) {
            for (group of category.groups) {
                for (unlock of group.content) {
                    lookup[typeId][unlock.id] = unlock
                }
            }
        }
    }
}

function addAcquisitionDetails(prefix) {
    var acquisitionDetails = $('#' + prefix + 'selection-acquisition-methods')
    for (method of acquisitionMethods) {
        if (!method.hideOnDetails) {
          acquisitionDetails.append('<div id="' + prefix + 'acquisition-' + method.id + '"><span class="base-icon ' + method.id + '-icon" role="img" aria-label="' + method.name + '"/></span>' + method.name)
        }
    }
}

// Core Utility

var acquisitionMethodsLookup = {}
for (method of acquisitionMethods) { acquisitionMethodsLookup[method.id] = method }

function gwuFilter(x) {return x.sources.includes('gwu')}
function bountyFilter(x) {return x.sources.includes('bounty')}

function isUnlocked(section, id) {
    return $('#' + section + '-' + id).hasClass('unlocked');
}

// Advanced Filters

function addAcquisitionFilters() {
  var acquisitionDetails = $('#advanced-filter-section')
    var filterSections = {};

    for (method of acquisitionMethods) {
      if (!method.hideOnDetails) {
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
    }
    var filterContent = "<details open='true'><summary class='advanced-filter-summary'>Advanced Filters...</summary>";
    filterContent += '<div class="filter-option"><div class="filter-label">Base Filter</div><div class="filter-selection-div"><select id="filter-base" name="filter-base" class="filter-selection-base"><option value="include" data-id="base">Include</option><option value="exclude" data-id="base" selected="selected">Exclude</option></select></div></div>'
    Object.keys(filterSections).forEach(function(key,index) {
        filterContent += filterSections[key];
        filterContent += '</div>';
    });
    filterContent += "</details>";
    acquisitionDetails.append(filterContent);

    var filter = $('.filter-selection').change(function() {
        updateFilter($('#filter-by-acquisition')[0].value, $('#filter-by-container')[0].value);
    });
    var filter = $('.filter-selection-base').change(function() {
        updateFilter($('#filter-by-acquisition')[0].value, $('#filter-by-container')[0].value);
    });
}

// Analyse Tab

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

  var filter = function (data) {return true;}
    if ($('#analyse-selection')[0].value == 'gwu') {
        filter = function(data) {return data.gwu;}
    } else if ($('#analyse-selection')[0].value == 'armor') {
        filter = function(data) {return data.gwu && data.section == 'armor';}
    } else if ($('#analyse-selection')[0].value == 'weapon') {
        filter = function(data) {return data.gwu && data.section == 'weapon';}
    } else if ($('#analyse-selection')[0].value == 'bounty') {
        filter = function(data) {return data.details.sources.includes('bounty');}
    }

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
                    if (!isNaN(data.price) && data.price < threshold && filter(data)) {
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

function updateFilter(displayMode, container) {
    $('.item').toggleClass('hidden', false);
    $('.section-groups').toggleClass('hidden', displayMode == 'tp-buy' || displayMode == 'tp-sell' || displayMode == 'buy' || displayMode == 'sell');
    $('.category').toggleClass('hidden', false);
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
    } else if (displayMode == 'free') {
        $('.item').not('.story,.achievement').toggleClass('hidden', true);
    } else if (displayMode == 'ibs') {
        $('.item').not('.ibs').toggleClass('hidden', true);
    } else if (displayMode == 'eod') {
        $('.item').not('.eod').toggleClass('hidden', true);
    } else if (displayMode == 'soto') {
        $('.item').not('.soto').toggleClass('hidden', true);
    } else if (displayMode == 'ktghb') {
        $('.item').not('.bounty').toggleClass('hidden', true);
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
    if (container == 'gwu') {
        $('.item').not('.gwu').toggleClass('hidden', true);
        $('#totals').toggleClass('hidden', true);
        $('#gwu-totals').toggleClass('hidden', false);
        $('#bounty-totals').toggleClass('hidden', true);
        $('.section-counts').toggleClass('hidden', true);
        $('.gwu-section-counts').toggleClass('hidden', false);
        $('.bounty-section-counts').toggleClass('hidden', true);
    } else if (container == 'bounty') {
        $('.item').not('.bounty').toggleClass('hidden', true);
        $('#totals').toggleClass('hidden',true);
        $('#gwu-totals').toggleClass('hidden', true);
        $('#bounty-totals').toggleClass('hidden', false);
        $('.section-counts').toggleClass('hidden', true);
        $('.gwu-section-counts').toggleClass('hidden', true);
        $('.bounty-section-counts').toggleClass('hidden', false);
    } else {
        $('#totals').toggleClass('hidden', false);
        $('#gwu-totals').toggleClass('hidden', true);
        $('#bounty-totals').toggleClass('hidden', true);
        $('.section-counts').toggleClass('hidden', false);
        $('.gwu-section-counts').toggleClass('hidden', true);
        $('.bounty-section-counts').toggleClass('hidden', true);
    }
    updateGroupVisibility();
    updateCounts();
    updateSectionFolding();
    if (storageAvailable('localStorage')) {
        localStorage.setItem('container', container);
        localStorage.setItem('filter', displayMode);
    }
}

function sortGroupsByBuyTotal() {
    $('.section-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return parseInt(a.dataset.buyTotal) - parseInt(b.dataset.buyTotal); }).appendTo(section)
    });
    $('.category-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return parseInt(a.dataset.buyTotal) - parseInt(b.dataset.buyTotal); }).appendTo(section)
    });
}

function sortGroupsBySellTotal() {
    $('.section-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return parseInt(a.dataset.sellTotal) - parseInt(b.dataset.sellTotal); }).appendTo(section)
    });
    $('.category-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return parseInt(a.dataset.sellTotal) - parseInt(b.dataset.sellTotal); }).appendTo(section)
    });
}

function sortGroupsByName() {
    $('.section-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return a.dataset.ordering - b.dataset.ordering; }).appendTo(section)
    });
    $('.category-groups').each(function (index, section) {
        $(section.children).sort(function (a, b) { return a.dataset.ordering - b.dataset.ordering; }).appendTo(section)
    });
}

function processAdvancedFilter() {
  if ($(".filter-selection-base option:selected")[0].value == "include") {
    $('.item').toggleClass('hidden', false);
    $(".filter-selection option:selected[value='exclude']").each(function (index, filter) {
      $('.item.' + $(filter).data("id")).toggleClass('hidden', true);
    });
    $(".filter-selection option:selected[value='include']").each(function (index, filter) {
      $('.item.' + $(filter).data("id")).toggleClass('hidden', false);
    });
  } else {
    $('.item').toggleClass('hidden', true);
    $(".filter-selection option:selected[value='include']").each(function (index, filter) {
      $('.item.' + $(filter).data("id")).toggleClass('hidden', false);
    });
    $(".filter-selection option:selected[value='exclude']").each(function (index, filter) {
      $('.item.' + $(filter).data("id")).toggleClass('hidden', true);
    });
  }
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
                    var bountyUnlockedBuyValue = 0;
                    var bountyUnlockedSellValue = 0;
                    var bountyUnlockedCount = 0;
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
                            if (icon.hasClass('bounty')) {
                                bountyUnlockedCount++;
                            }
                        }
                        var buyicon = $('#' + sectionData.id + '-buy-tp-' + id).toggleClass('unlocked', true);
                        var sellicon = $('#' + sectionData.id + '-sell-tp-' + id).toggleClass('unlocked', true);
                        if (sectionBuyPrices[id]) {
                            unlockedBuyValue += sectionBuyPrices[id].price;
                            if (icon.hasClass('gwu')) {
                                gwuUnlockedBuyValue += sectionBuyPrices[id].price;
                            }
                            if (icon.hasClass('bounty')) {
                                bountyUnlockedBuyValue += sectionBuyPrices[id].price;
                            }
                        }
                        if (sectionSellPrices[id]) {
                            unlockedSellValue += sectionSellPrices[id].price;
                            if (icon.hasClass('gwu')) {
                                gwuUnlockedSellValue += sectionSellPrices[id].price;
                            }
                            if (icon.hasClass('bounty')) {
                                bountyUnlockedSellValue += sectionSellPrices[id].price;
                            }
                        }
                    }

                    counts[sectionData.id].unlocked = unlockedCount;
                    counts[sectionData.id].unlockedBuyValue = unlockedBuyValue;
                    counts[sectionData.id].unlockedSellValue = unlockedSellValue;
                    gwuCounts[sectionData.id].unlocked = gwuUnlockedCount;
                    gwuCounts[sectionData.id].unlockedBuyValue = gwuUnlockedBuyValue;
                    gwuCounts[sectionData.id].unlockedSellValue = gwuUnlockedSellValue;
                    bountyCounts[sectionData.id].unlocked = bountyUnlockedCount;
                    bountyCounts[sectionData.id].unlockedBuyValue = bountyUnlockedBuyValue;
                    bountyCounts[sectionData.id].unlockedSellValue = bountyUnlockedSellValue;

                    updateCounts();
                    updateGroupVisibility();
                    updateThresholdCalculation();
                }.bind(null, sectionData),
                error: function(val) {
                    $('#api-error').toggleClass('hidden', false);
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
    updateCountsType('bounty-', bountyCounts);
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
    for (var categoryIndex = 0; categoryIndex < sectionData.categories.length; ++categoryIndex) {
        var categoryName = sectionData.categories[categoryIndex].name;
        for (var groupIndex = 0; groupIndex < sectionData.categories[categoryIndex].groups.length; ++groupIndex) {
            var groupName = sectionData.categories[categoryIndex].groups[groupIndex].groupName;
            for (var itemIndex = 0; itemIndex < sectionData.categories[categoryIndex].groups[groupIndex].content.length; ++itemIndex) {
                sectionData.categories[categoryIndex].groups[groupIndex].content[itemIndex].categoryName = categoryName;
                sectionData.categories[categoryIndex].groups[groupIndex].content[itemIndex].groupName = groupName;
            }
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
    var bountyCount = 0;
    for (var categoryIndex = 0; categoryIndex < sectionData.categories.length; ++categoryIndex) {
        for (var groupIndex = 0; groupIndex < sectionData.categories[categoryIndex].groups.length; ++groupIndex) {
            count += sectionData.categories[categoryIndex].groups[groupIndex].content.length;
            gwuCount += sectionData.categories[categoryIndex].groups[groupIndex].content.filter(gwuFilter).length;
            bountyCount += sectionData.categories[categoryIndex].groups[groupIndex].content.filter(bountyFilter).length;
        }
    }
    var countData = {};
    countData.total = count;
    countData.unlocked = 0;
    counts[sectionData.id] = countData;
    var gwuCountData = {};
    gwuCountData.total = gwuCount;
    gwuCountData.unlocked = 0;
    gwuCounts[sectionData.id] = gwuCountData;
    var bountyCountData = {};
    bountyCountData.total = bountyCount;
    bountyCountData.unlocked = 0;
    bountyCounts[sectionData.id] = bountyCountData;

    var tpItems = extractTPItems(sectionData);

    var groups = buildCategories(sectionData);
    var sellSection = buildTPSorted(sectionData.id, tpItems, sellPriceLookup, getSellPrice, 'sell');
    var buySection = buildTPSorted(sectionData.id, tpItems, buyPriceLookup, getBuyPrice, 'buy');

    countData.totalBuyValue = calculateTotalValue(tpItems, getBuyPrice);
    countData.totalSellValue = calculateTotalValue(tpItems, getSellPrice);
    countData.unlockedBuyValue = 0;
    countData.unlockedSellValue = 0;
    countData.prefix = '';

    gwuCountData.totalBuyValue = calculateTotalValue(tpItems.filter(gwuFilter), getBuyPrice);
    gwuCountData.totalSellValue = calculateTotalValue(tpItems.filter(gwuFilter), getSellPrice);
    gwuCountData.unlockedBuyValue = 0;
    gwuCountData.unlockedSellValue = 0;
    gwuCountData.prefix = 'gwu-';

    bountyCountData.totalBuyValue = calculateTotalValue(tpItems.filter(bountyFilter), getBuyPrice);
    bountyCountData.totalSellValue = calculateTotalValue(tpItems.filter(bountyFilter), getSellPrice);
    bountyCountData.unlockedBuyValue = 0;
    bountyCountData.unlockedSellValue = 0;
    bountyCountData.prefix = 'bounty-'

    countTypes = [countData, gwuCountData, bountyCountData];

    if (!sectionData.unlockUrl) {
      section += '<p>Note: Account unlock information not available for this section</p>';
    } else {
      for (var countIndex = 0; countIndex < countTypes.length; ++countIndex) {
          var type = countTypes[countIndex];
          section += '<div class="' + type.prefix + 'section-counts">';
          section += '<p id="' + sectionData.id + '-' + type.prefix + 'count">Unlocked: <span id="' + sectionData.id + '-' + type.prefix + 'unlocked-count">0</span> / <span id="' + sectionData.id + '-' + type.prefix + 'total-count">' + type.total + '</span></p>';
          section += '<p>Unlocked by buy value: <span id="' + sectionData.id + '-' + type.prefix + 'buy-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-' + type.prefix + 'buy-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-' + type.prefix + 'buy-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of '
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'buy-total-gold">' + Math.floor(type.totalBuyValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'buy-total-silver">' + (Math.floor(type.totalBuyValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'buy-total-copper">' + (type.totalBuyValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></p>';

          section += '<p>Unlocked by sell value: <span id="' + sectionData.id + '-' + type.prefix + 'sell-unlocked-gold">0</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> <span id="' + sectionData.id + '-' + type.prefix + 'sell-unlocked-silver">0</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> <span id="' + sectionData.id + '-' + type.prefix + 'sell-unlocked-copper">0</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span> of ';
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'sell-total-gold">' + Math.floor(type.totalSellValue / 10000) + '</span><span class="base-icon gold-icon" role="img" aria-label="Gold"></span> ';
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'sell-total-silver">' + (Math.floor(type.totalSellValue / 100) % 100) + '</span><span class="base-icon silver-icon" role="img" aria-label="Silver"></span> ';
          section += '<span id="' + sectionData.id + '-' + type.prefix + 'sell-total-copper">' + (type.totalSellValue % 100) + '</span><span class="base-icon copper-icon" role="img" aria-label="Copper"></span></p>';
          section += '</div>';
      }
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


    for (var categoryIndex = 0; categoryIndex < sectionData.categories.length; ++categoryIndex) {
        for (var groupIndex = 0; groupIndex < sectionData.categories[categoryIndex].groups.length; ++groupIndex) {
            var groupData = sectionData.categories[categoryIndex].groups[groupIndex];
            setupClickHandlers(sectionData.id, groupData.content)
        }
    }

    setupClickHandlers(sectionData.id + '-buy-tp', tpItems);
    setupClickHandlers(sectionData.id + '-sell-tp', tpItems);
}

function buildCategories(sectionData) {
    var categories = '';
    if (sectionData.categories.length > 1) {
        for (var categoryIndex = 0; categoryIndex < sectionData.categories.length; ++categoryIndex) {
            var buyTotal = 0;
            var sellTotal = 0;
            for (var groupIndex = 0; groupIndex < sectionData.categories[categoryIndex].groups.length; ++groupIndex) {
              buyTotal += calculateTotalValue(sectionData.categories[categoryIndex].groups[groupIndex].content, getBuyPrice);
              sellTotal += calculateTotalValue(sectionData.categories[categoryIndex].groups[groupIndex].content, getSellPrice);
            }

            categories += '<details id="' + sectionData.id + '-' + sectionData.categories[categoryIndex].name + '" class="category" open="open" data-buy-total="' + buyTotal + '" data-sell-total="' + sellTotal + '" data-ordering = "' + categoryIndex + '">';
            categories += '<summary class="category-header">' + sectionData.categories[categoryIndex].name + '</summary>';
            categories += '<div class="category-groups">';
            categories += buildSectionGroups(sectionData.categories[categoryIndex], sectionData.id);
            categories += '</div></details>';
        }
    } else {
        categories += buildSectionGroups(sectionData.categories[0], sectionData.id);
    }
    return categories;
}

function buildSectionGroups(groupsData, typeId) {
    var groups = '';
    for (var groupIndex = 0; groupIndex < groupsData.groups.length; ++groupIndex) {
        var groupData = groupsData.groups[groupIndex];
        var buyTotal = calculateTotalValue(groupData.content, getBuyPrice);
        var sellTotal = calculateTotalValue(groupData.content, getSellPrice);

        var group = '<div class="group" data-buy-total="' + buyTotal + '" data-sell-total="' + sellTotal + '" data-ordering = "' + groupIndex + '"><h3>' + htmlEscape(groupData.groupName) + '</h3>';
        group += '<div class="section-body">';
        group += populateContent(groupData.content, typeId);
        group += '</div></div>';
        groups += group;
    }
    return groups;
}

function extractTPItems(sectionData) {
    var tpItems = [];
    for (var categoryIndex = 0; categoryIndex < sectionData.categories.length; ++categoryIndex) {
        for (var groupIndex = 0; groupIndex < sectionData.categories[categoryIndex].groups.length; ++groupIndex) {
            var groupData = sectionData.categories[categoryIndex].groups[groupIndex];
            for (var itemIndex = 0; itemIndex < groupData.content.length; ++itemIndex) {
                if (groupData.content[itemIndex].priceData || !isNaN(getVendorPrice(groupData.content[itemIndex]))) {
                    tpItems.push(groupData.content[itemIndex]);
                }
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
    var categories = $('.category');
    for (var i = 0; i < categories.length; ++i) {
        $(categories[i]).toggle($(categories[i]).find('.item').not('.unlocked').not('.hidden').length > 0);
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
    if (itemData.linkedUnlocks.length > 0) {
        result += ' linked';
    }

    if ($.inArray('winterberries', itemData.sources) != -1 || $.inArray('unboundmagic', itemData.sources) != -1 || $.inArray('petrifiedwood', itemData.sources) != -1 || $.inArray('fireorchidblossom', itemData.sources) != -1 || $.inArray('orrianpearl', itemData.sources) != -1 || $.inArray('jadeshard', itemData.sources) != -1) {
        result += ' ls3';
    }
    if ($.inArray('volatilemagic', itemData.sources) != -1 || $.inArray('kralkatiteore', itemData.sources) != -1 || $.inArray('difluorite', itemData.sources) != -1 || $.inArray('swimspeedinfusion', itemData.sources) != -1 || $.inArray('mistonium', itemData.sources) != -1 || $.inArray('inscribedshard', itemData.sources) != -1 || $.inArray('brandedmass', itemData.sources) != -1 || $.inArray('mistbornmote', itemData.sources) != -1) {
        result += ' ls4';
    }
    if ($.inArray('hatchedchili', itemData.sources) != -1 || $.inArray('eternaliceshard', itemData.sources) != -1 || $.inArray('eitriteingot', itemData.sources) != -1 || $.inArray('tyriandefenseseal', itemData.sources) != -1) {
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
    if (itemData.linkedUnlocks.length > 0) {
        result += '<span class="base-icon linked-icon" role="img" aria-label="Linked"></span>';
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
        item += '<div class="name">' + htmlEscape(itemData.name) + '</div>';
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
    if (item.type) {
        $('#' + prefix + 'selection-type').children('span').text(item.type);
        $('#' + prefix + 'selection-type').toggle(true);
    } else {
        $('#' + prefix + 'selection-type').toggle(false);
    }
    $('#' + prefix + 'selection-group').children('span').text(item.groupName);
    if (item.type) {
        $('#' + prefix + 'selection-type').children('span').text(item.type);
        if (item.subtype) {
            $('#' + prefix + 'selection-subtype').children('span').text(item.subtype);
        } else {
            $('#' + prefix + 'selection-subtype').children('span').text('None');
        }
    } else {
        $('#' + prefix + 'selection-type').children('span').text('Unknown');
        $('#' + prefix + 'selection-subtype').children('span').text('Unknown');
    }
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

        if (item.linkedUnlocks.length > 0) {
            $('#' + prefix + 'selection-unlocked-by').toggle(true);
            var unlockedByList = $('#' + prefix + 'selection-unlocked-by-list');
            unlockedByList.empty();
            for (unlockLink of item.linkedUnlocks) {
                var unlockData = lookup[unlockLink.type][unlockLink.id]
                var link = $('<div><a href="" target="_blank">' + unlockData.name + '</a></div>')
                link.click(function(item, prefix) {
                    showDetails(item, prefix);
                    return false;
                }.bind(null, unlockData, prefix));
                unlockedByList.append(link)
            }
        } else {
            $('#' + prefix + 'selection-unlocked-by').toggle(false);
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

function htmlEscape(text) {
    return text.replace('<', '&lt;');
}
