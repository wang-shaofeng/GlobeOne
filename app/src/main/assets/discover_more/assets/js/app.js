require=(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
"use strict";

var _jquery = require("jquery");

var _jquery2 = _interopRequireDefault(_jquery);

var _lodash = require("lodash");

var _lodash2 = _interopRequireDefault(_lodash);

var _eq2 = require("eq");

var _eq3 = _interopRequireDefault(_eq2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var partials = require("partials-template"),
    tagsToReplace = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;"
};

var visibleSections = {
    all: true,
    global: true,
    reference: true,
    component: true,
    modifier: true
};

_lodash2.default.mixin({
    sortKeysBy: function sortKeysBy(obj, comparator) {
        var keys = _lodash2.default.sortBy(_lodash2.default.keys(obj), function (key) {
            return comparator ? comparator(obj[key], key) : key;
        });

        var sortedObject = {};
        _lodash2.default.each(keys, function (key) {
            sortedObject[key] = obj[key];
        });

        return sortedObject;
    }
});

var safeTagReplace = function safeTagReplace(str) {
    return str.replace(/[&<>]/g, function (tag) {
        return tagsToReplace[tag] || tag;
    });
};

/**
 * Discover declared elements
 * 
 * @param {*} Elements block
 * @param {*} includeHTML 
 * @param {*} index 
 */
var discoverElements = function discoverElements($elements, includeHTML, index) {

    _lodash2.default.each($elements, function (element) {
        var $el = (0, _jquery2.default)(element);
        var className = $el.attr("class");
        var classes = className.split(" ");

        var isIgnored = _lodash2.default.indexOf(classes, "--ignore") > -1,
            shouldExplore = _lodash2.default.indexOf(classes, "--explore") > -1;

        var name = $el.attr("data-name");

        className = typeof name !== "undefined" ? name : className;

        if (!isIgnored) {

            var $wrappedEl = $el.wrap("<div class=\"--element\" data-class-name=\"" + className + "\"/>");

            $el = $el.wrap('<div class="--wrap"/>');

            var isComponent = false,
                indexDetail = {
                name: className
            };

            $el.before("<a name=\"" + className + "\" />");

            // Include HTML as output
            if (includeHTML) {
                var html = safeTagReplace(window.html_beautify($el[0].outerHTML, {
                    indent_size: 4,
                    end_with_newline: true,
                    wrap_line_length: 40
                }));

                $wrappedEl.parent().after(partials.codeView(html));
                isComponent = true;
            }

            index.push(indexDetail);
        }

        if (shouldExplore) {
            discoverElements($el.find("> *"), includeHTML, index);
        }
    });

    return index;
};

/**
 * Add Elements to index
 * 
 * @param {*}  
 * @param {*} classIndex 
 */
var addIndex = function addIndex($indexEl, classIndex) {
    _lodash2.default.each(classIndex, function (index) {
        $indexEl.append(partials.indexItem(index.name));
    });
};

/**
 * Group mixins based on components (for component modifiers)
 * 
 * @param {*} mixins 
 */
var groupMixins = function groupMixins(mixins) {
    var modifierPattern = /(.+)?\-\-(.+)?/gi;

    var mixinsGroup = {
        globals: [],
        modifiers: {}
    };

    _lodash2.default.each(mixins, function (mixin) {
        if (mixin.name.match(modifierPattern)) {

            var mixinDetail = modifierPattern.exec(mixin.name),
                component = mixinDetail[1];

            if (!mixinsGroup.modifiers.hasOwnProperty(component)) {
                mixinsGroup.modifiers[component] = [];
            }

            mixinsGroup.modifiers[component].push(mixin);
        } else {
            mixinsGroup.globals.push(mixin);
        }
    });

    mixinsGroup.modifiers = _lodash2.default.sortKeysBy(mixinsGroup.modifiers);

    return mixinsGroup;
};

/*-----------------------------------------------------------------------------*/
/**
 * Rendering partial and injecting it into the DOM
 * 
 * @param {*} selector 
 * @param {*} renderedPartial 
 */
var renderPartial = function renderPartial(selector, renderedPartial) {
    (0, _jquery2.default)(selector).find(".section-content").append(renderedPartial);
};

/**
 * Join rendered partials and stringify it's content
 * 
 * @param {*} partials 
 */
var renderPartialBlocks = function renderPartialBlocks(partials) {
    return partials.join("");
};

/**
 * Render mixin and it's parameters
 * 
 * @param {*} mixin 
 */
var renderMixinItem = function renderMixinItem(mixin) {

    var parameters = renderPartialBlocks(_lodash2.default.map(mixin.parameters, function (param) {
        return "<span>" + param + "</span>";
    }));

    return partials.mixinItem({
        name: mixin.name,
        parameter: parameters
    });
};

/**
 * Render a list of mixins
 * 
 * @param {*} mixins 
 */
var renderMixinTable = function renderMixinTable(mixins) {
    return renderPartialBlocks(_lodash2.default.map(mixins, function (mixin) {
        return renderMixinItem(mixin);
    }));
};

/**
 * Render sections from the Design Guidelines
 */
var renderSections = function renderSections() {

    var designTokens = _lodash2.default.sortKeysBy(require("./design-token.json")),
        sassReferences = require("./exodus-references.json");

    sassReferences.mixins = _lodash2.default.sortKeysBy(sassReferences.mixins);

    var $elementGroups = (0, _jquery2.default)(".element-group"),
        $indexes = (0, _jquery2.default)(".index-group"),
        variables = sassReferences.globals,
        groupedMixins = groupMixins(sassReferences.mixins),
        mixins = groupedMixins.globals,
        modifiers = groupedMixins.modifiers,
        $tokenGroups = (0, _jquery2.default)(".--map-to-tokens");

    /* Add section block title */
    _lodash2.default.each((0, _jquery2.default)('.section-block'), function (section) {
        var $section = (0, _jquery2.default)(section),
            name = $section.attr('data-name');
        $section.prepend("<h2 class=\"name\">" + name + "</h2>");
    });

    /* Generate elements block */
    _lodash2.default.each($elementGroups, function (group) {
        var $group = (0, _jquery2.default)(group),
            name = $group.attr('data-name'),
            showCode = $group.hasClass('--show-code');

        var $elements = $group.find('.element > *');
        var index = discoverElements($elements, showCode, []);

        $indexes.append("<ul class=\"index-list\" data-label=\"" + name + "\"></ul>");

        addIndex((0, _jquery2.default)('.index-list[data-label="' + name + '"]'), index);
    });

    ////////////////////////////////////////////////////////
    /* Design Token specific block */
    _lodash2.default.each($tokenGroups, function (group) {
        var $group = (0, _jquery2.default)(group),
            name = $group.attr("data-name"),
            tokenIndex = $group.attr("data-token-index"),
            tokens = [];

        _lodash2.default.each(designTokens[tokenIndex], function (value, name) {
            value = _lodash2.default.find(variables, {
                variable: "$" + name + "-" + tokenIndex
            });

            tokens.push({ name: name, value: value.compiledValue });
        });

        // if there is a token block
        if (partials.hasOwnProperty(tokenIndex)) {
            $group.append(partials.tokenBlock({
                name: name,
                tpl: renderPartialBlocks(_lodash2.default.map(tokens, function (token) {
                    return partials[tokenIndex](token);
                }))
            }));
        }
    });

    ////////////////////////////////////////////////////////
    /** Global Variable block  */
    renderPartial(".global-variables", renderPartialBlocks(_lodash2.default.map(designTokens, function (tokens, scope) {

        tokens = _lodash2.default.map(tokens, function (value, variable) {
            variable = _lodash2.default.find(variables, {
                variable: "$" + variable + (scope != '' ? "-" + scope : '')
            });

            if (variable.compiledValue.match(/solid/)) {
                variable.compiledValue = variable.compiledValue.replace('solid', ' solid ');
            }

            return partials.variableItem(variable);
        });

        return partials.groupTableBlock({
            group: scope ? scope : '&mdash;',
            tpl: renderPartialBlocks(tokens)
        });
    })));

    ////////////////////////////////////////////////////////
    /** Global mixins block */
    renderPartial(".global-mixins", partials.tableDataBlock({
        tpl: renderMixinTable(mixins)
    }));

    ////////////////////////////////////////////////////////
    /** Component Modifiers block */
    renderPartial(".component-modifiers", renderPartialBlocks(_lodash2.default.map(modifiers, function (componentModifiers, component) {
        return partials.groupTableBlock({
            group: component,
            tpl: renderMixinTable(componentModifiers)
        });
    })));
};

var toggleSectionVisibility = function toggleSectionVisibility(targetSection) {

    var $displayPanels = (0, _jquery2.default)('.display-panels');

    if (targetSection === 'all') {
        // activate all blocks
        _lodash2.default.each(visibleSections, function (isVisible, section) {
            visibleSections[section] = true;
        });

        $displayPanels.addClass('-show-index');
    } else {
        // reset view first
        _lodash2.default.each(visibleSections, function (isVisible, section) {
            visibleSections[section] = false;
        });

        // toggle visibility of that section
        visibleSections[targetSection] = true;
        // only show index if the target section is: component
        $displayPanels.toggleClass('-show-index', targetSection === 'component');
    }

    // show only the visible sections
    _lodash2.default.each(visibleSections, function (isVisible, section) {
        (0, _jquery2.default)(".section-link[data-target=\"" + section + "\"]").toggleClass('-active', isVisible);
        (0, _jquery2.default)(".section-block.-" + section).toggleClass('-section-is-hidden', !isVisible);
    });

    (0, _jquery2.default)('body,html').scrollTop(0);
};

/**
 * Bind Events
 */
var bindEvents = function bindEvents() {

    // time to read all the classes
    var $previewPane = (0, _jquery2.default)(".preview-pane");

    ////////////////////////////////////////////////////////
    // toggle global code view
    (0, _jquery2.default)('.toggle-code-view').on('click', function (e) {
        var $el = (0, _jquery2.default)(e.currentTarget).find(".switch"),
            codeIsVisible = $el.hasClass("-active");

        $el.toggleClass("-active");
        $previewPane.toggleClass('-hide-code', codeIsVisible);
        (0, _jquery2.default)(".--element").removeClass("-code-visible");
    });

    (0, _jquery2.default)('.code-viewer').on('click', function (e) {
        if ($previewPane.hasClass('-hide-code')) {
            var $el = (0, _jquery2.default)(e.currentTarget).parent();
            $el.toggleClass("-code-visible");
        }
    });

    (0, _jquery2.default)('.section-link-group > .section-link').on('click', function (e) {
        var $el = (0, _jquery2.default)(e.currentTarget);

        toggleSectionVisibility($el.attr("data-target"));
    });

    (0, _jquery2.default)('.toggle-sidebar').on('click', function (e) {
        var $el = (0, _jquery2.default)(e.currentTarget).find(".switch");

        $el.toggleClass("-active");
        (0, _jquery2.default)(".index-group").toggleClass('-hide-index');
        (0, _jquery2.default)(".control-bar").toggleClass('-full-width');
        (0, _jquery2.default)(".preview-pane").toggleClass('-full-width');
    });
};

/////////////////////////////////////////
var scrollToTop = function scrollToTop() {
    (0, _jquery2.default)('.-scroll-to-top').on('click', function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
};

(function () {
    renderSections();
    toggleSectionVisibility('all');
    bindEvents();

    scrollToTop();
})();

// Responsive Pagination
////////////////////////////////////////

var $pagination = (0, _jquery2.default)('.page-pagination');

if ((0, _jquery2.default)($pagination).width() > 1140) {
    $pagination.addClass('responsive-pagination');
}

},{"./design-token.json":2,"./exodus-references.json":3,"eq":"eq","jquery":"jquery","lodash":"lodash","partials-template":"partials-template"}],2:[function(require,module,exports){
module.exports={
    "color": {
        "blue": "#0035AD"

    },

    "border": {
        "faint-dark-hairline": "1px solid #000"

    },

    "space": {
        "quarter": "2px",
        "half": "4px",
        "default": "8px",
        "secondary": "12px",
        "double": "16px",
        "triple": "24px",
        "quad": "32px",
        "double-triple": "48px",
        "double-quad": "64px",
        "big": "72px",
        "wide": "96px"
    },

    "padding": {
        "clear": "0",
        "full": "$quad-space"
    },

    "position": {
        "centered": "auto",
        "lateral-centered": "0 auto",
        "vertically-centered": "auto 0",
        "push-the-right": "0 0 0 auto",
        "push-the-left": "0 auto 0 0"
    },

    "font": {
        "ui": "\"Libre Baskerville\", Helvetica, sans-serif",
        "body": "\"Karla\", Arial, sans-serif",
        "neue-extendedbold": "\"Neue Plak-ExtendedBold\", Helvetica",
        "neue-widebold": "\"Neue Plak-WideBold\", Helvetica"
    },

    "font-weight": {
        "light": 300,
        "regular": 400,
        "semi-bold": 600,
        "bold": 700
    },

    "font-size": {
        "triple-xl"    : "4rem",
        "dobule-xl"    : "3.250rem",
        "xl"     : "2.875rem",
        "large"  : "2em",
        "medium" : "1.750em",
        "small"  : "1.375em",
        "base"   : "1.125em",
        "xs"     : "1em",
        "double-xs"    : "0.875em"
    },

    "letter-tracking": {
        "wide": "0.18em",
        "tight": "0.12em"
    },

    "icon-size": {
        "tiny": "8px",
        "smallest": "10px",
        "small": "12px",
        "default": "16px",
        "medium": "18px",
        "double-medium": "36px",
        "large": "24px",
        "largest": "32px"
    },

    "": {
        "wrapper-width" : "1440px",
        "max-container-width": "1600px",
        "icon-size": "24px",
        "icon-sprite-file": "'../images/icons-sprite-sheet.svg'",
        "icon-column-count": 12,
        "icon-row-count": 12,

        "fully-opaque": 1,
        "opaque": 0.8,
        "slightly-opaque": 0.5,
        "transparent": 0.2,
        "almost-transparent": 0.1,
        "fully-transparent": 0,
        "amount": 0,
        "gradient-spread": "100%",
        "overlay-height": "40%",
        "overlay-transparency": 1,
        "content-limit": "768px"
    }
}
},{}],3:[function(require,module,exports){
module.exports={
  "globals": [
    {
      "variable": "$blue-color",
      "value": "#0035AD",
      "compiledValue": "#0035AD"
    },
    {
      "variable": "$faint-dark-hairline-border",
      "value": "1px solid #000",
      "compiledValue": "1pxsolid#000"
    },
    {
      "variable": "$quarter-space",
      "value": "2px",
      "compiledValue": "2px"
    },
    {
      "variable": "$half-space",
      "value": "4px",
      "compiledValue": "4px"
    },
    {
      "variable": "$default-space",
      "value": "8px",
      "compiledValue": "8px"
    },
    {
      "variable": "$secondary-space",
      "value": "12px",
      "compiledValue": "12px"
    },
    {
      "variable": "$double-space",
      "value": "16px",
      "compiledValue": "16px"
    },
    {
      "variable": "$triple-space",
      "value": "24px",
      "compiledValue": "24px"
    },
    {
      "variable": "$quad-space",
      "value": "32px",
      "compiledValue": "32px"
    },
    {
      "variable": "$double-triple-space",
      "value": "48px",
      "compiledValue": "48px"
    },
    {
      "variable": "$double-quad-space",
      "value": "64px",
      "compiledValue": "64px"
    },
    {
      "variable": "$big-space",
      "value": "72px",
      "compiledValue": "72px"
    },
    {
      "variable": "$wide-space",
      "value": "96px",
      "compiledValue": "96px"
    },
    {
      "variable": "$clear-padding",
      "value": "0",
      "compiledValue": "0"
    },
    {
      "variable": "$full-padding",
      "value": "$quad-space",
      "compiledValue": "32px"
    },
    {
      "variable": "$centered-position",
      "value": "auto",
      "compiledValue": "auto"
    },
    {
      "variable": "$lateral-centered-position",
      "value": "0 auto",
      "compiledValue": "0auto"
    },
    {
      "variable": "$vertically-centered-position",
      "value": "auto 0",
      "compiledValue": "auto0"
    },
    {
      "variable": "$push-the-right-position",
      "value": "0 0 0 auto",
      "compiledValue": "000auto"
    },
    {
      "variable": "$push-the-left-position",
      "value": "0 auto 0 0",
      "compiledValue": "0auto00"
    },
    {
      "variable": "$ui-font",
      "value": "Libre Baskerville, Helvetica, sans-serif",
      "compiledValue": "LibreBaskerville,Helvetica,sans-serif"
    },
    {
      "variable": "$body-font",
      "value": "Karla, Arial, sans-serif",
      "compiledValue": "Karla,Arial,sans-serif"
    },
    {
      "variable": "$neue-extendedbold-font",
      "value": "Neue Plak-ExtendedBold, Helvetica",
      "compiledValue": "NeuePlak-ExtendedBold,Helvetica"
    },
    {
      "variable": "$neue-widebold-font",
      "value": "Neue Plak-WideBold, Helvetica",
      "compiledValue": "NeuePlak-WideBold,Helvetica"
    },
    {
      "variable": "$light-font-weight",
      "value": "300",
      "compiledValue": "300"
    },
    {
      "variable": "$regular-font-weight",
      "value": "400",
      "compiledValue": "400"
    },
    {
      "variable": "$semi-bold-font-weight",
      "value": "600",
      "compiledValue": "600"
    },
    {
      "variable": "$bold-font-weight",
      "value": "700",
      "compiledValue": "700"
    },
    {
      "variable": "$triple-xl-font-size",
      "value": "4rem",
      "compiledValue": "4rem"
    },
    {
      "variable": "$dobule-xl-font-size",
      "value": "3.250rem",
      "compiledValue": "3.25rem"
    },
    {
      "variable": "$xl-font-size",
      "value": "2.875rem",
      "compiledValue": "2.875rem"
    },
    {
      "variable": "$large-font-size",
      "value": "2em",
      "compiledValue": "2em"
    },
    {
      "variable": "$medium-font-size",
      "value": "1.750em",
      "compiledValue": "1.75em"
    },
    {
      "variable": "$small-font-size",
      "value": "1.375em",
      "compiledValue": "1.375em"
    },
    {
      "variable": "$base-font-size",
      "value": "1.125em",
      "compiledValue": "1.125em"
    },
    {
      "variable": "$xs-font-size",
      "value": "1em",
      "compiledValue": "1em"
    },
    {
      "variable": "$double-xs-font-size",
      "value": "0.875em",
      "compiledValue": "0.875em"
    },
    {
      "variable": "$wide-letter-tracking",
      "value": "0.18em",
      "compiledValue": "0.18em"
    },
    {
      "variable": "$tight-letter-tracking",
      "value": "0.12em",
      "compiledValue": "0.12em"
    },
    {
      "variable": "$tiny-icon-size",
      "value": "8px",
      "compiledValue": "8px"
    },
    {
      "variable": "$smallest-icon-size",
      "value": "10px",
      "compiledValue": "10px"
    },
    {
      "variable": "$small-icon-size",
      "value": "12px",
      "compiledValue": "12px"
    },
    {
      "variable": "$default-icon-size",
      "value": "16px",
      "compiledValue": "16px"
    },
    {
      "variable": "$medium-icon-size",
      "value": "18px",
      "compiledValue": "18px"
    },
    {
      "variable": "$double-medium-icon-size",
      "value": "36px",
      "compiledValue": "36px"
    },
    {
      "variable": "$large-icon-size",
      "value": "24px",
      "compiledValue": "24px"
    },
    {
      "variable": "$largest-icon-size",
      "value": "32px",
      "compiledValue": "32px"
    },
    {
      "variable": "$wrapper-width",
      "value": "1440px",
      "compiledValue": "1440px"
    },
    {
      "variable": "$max-container-width",
      "value": "1600px",
      "compiledValue": "1600px"
    },
    {
      "variable": "$icon-size",
      "value": "24px",
      "compiledValue": "24px"
    },
    {
      "variable": "$icon-sprite-file",
      "value": "'../images/icons-sprite-sheet.svg'",
      "compiledValue": "../images/icons-sprite-sheet.svg"
    },
    {
      "variable": "$icon-column-count",
      "value": "12",
      "compiledValue": "12"
    },
    {
      "variable": "$icon-row-count",
      "value": "12",
      "compiledValue": "12"
    },
    {
      "variable": "$fully-opaque",
      "value": "1",
      "compiledValue": "1"
    },
    {
      "variable": "$opaque",
      "value": "0.8",
      "compiledValue": "0.8"
    },
    {
      "variable": "$slightly-opaque",
      "value": "0.5",
      "compiledValue": "0.5"
    },
    {
      "variable": "$transparent",
      "value": "0.2",
      "compiledValue": "0.2"
    },
    {
      "variable": "$almost-transparent",
      "value": "0.1",
      "compiledValue": "0.1"
    },
    {
      "variable": "$fully-transparent",
      "value": "0",
      "compiledValue": "0"
    },
    {
      "variable": "$amount",
      "value": "0",
      "compiledValue": "0"
    },
    {
      "variable": "$gradient-spread",
      "value": "100%",
      "compiledValue": "100%"
    },
    {
      "variable": "$overlay-height",
      "value": "40%",
      "compiledValue": "40%"
    },
    {
      "variable": "$overlay-transparency",
      "value": "1",
      "compiledValue": "1"
    },
    {
      "variable": "$content-limit",
      "value": "768px",
      "compiledValue": "768px"
    },
    {
      "variable": "$red",
      "value": "transparentize($color: red, $amount: 0.75)",
      "mapValue": [
        {
          "variable": "color",
          "value": "red",
          "compiledValue": "red"
        },
        {
          "variable": "amount",
          "value": "0.75",
          "compiledValue": "0.75"
        }
      ],
      "compiledValue": "rgba(255,0,0,0.25)"
    },
    {
      "variable": "$position",
      "value": "left",
      "compiledValue": "left"
    },
    {
      "variable": "$size",
      "value": "$default-icon-size, $position:left) {text-decoration: none",
      "mapValue": [
        {
          "variable": "position",
          "value": "left",
          "compiledValue": "left"
        },
        {
          "variable": "text-decoration",
          "value": "none",
          "compiledValue": "none"
        }
      ],
      "compiledValue": "16px,left"
    }
  ],
  "mixins": [
    {
      "name": "debug",
      "parameters": []
    },
    {
      "name": "vertical-display",
      "parameters": []
    },
    {
      "name": "horizontal-display",
      "parameters": []
    },
    {
      "name": "rotate-180deg",
      "parameters": []
    },
    {
      "name": "rotate-element",
      "parameters": [
        "$rotate-value : 90deg"
      ]
    },
    {
      "name": "visually-hidden",
      "parameters": []
    },
    {
      "name": "on",
      "parameters": [
        "$media"
      ]
    },
    {
      "name": "icon",
      "parameters": [
        "$col",
        "$row",
        "$size:$icon-size",
        "$is-responsive:false"
      ]
    },
    {
      "name": "square",
      "parameters": [
        "$size"
      ]
    },
    {
      "name": "square-icon",
      "parameters": [
        "$col",
        "$row",
        "$size:$icon-size"
      ]
    },
    {
      "name": "labeled-icon",
      "parameters": [
        "$col",
        "$row",
        "$size:$default-icon-size",
        "$position:left"
      ]
    },
    {
      "name": "hide-icon",
      "parameters": []
    },
    {
      "name": "wrapper-no-paddings-margin",
      "parameters": []
    },
    {
      "name": "wrapper",
      "parameters": [
        "$offset:0"
      ]
    },
    {
      "name": "wrapper-no-paddings",
      "parameters": []
    },
    {
      "name": "wrapped-content--clear-side-padding",
      "parameters": []
    },
    {
      "name": "side-paddings",
      "parameters": []
    }
  ]
}
},{}],"partials-template":[function(require,module,exports){
"use strict";

module.exports = {
    color: function color(data) {
        return "<div class=\"data\" data-name=\"$" + data.name + "-color\" data-value=\"" + data.value + "\">\n            <span style=\"background-color:" + data.value + "\" \n                    data-value=\"" + data.value + "\">\n            </span>\n        </div>";
    },

    space: function space(data) {
        return "\n        <div class=\"data\" data-name=\"$" + data.name + "-space\" data-value=\"" + data.value + "\">\n            <span style=\"width:" + data.value + "; height:" + data.value + ";\"></span>\n        </div>";
    },

    "icon-size": function iconSize(data) {
        return "\n        <div class=\"data\" data-name=\"$" + data.name + "-icon-size\" data-value=\"" + data.value + "\">\n            <span style=\"width:" + data.value + "; height:" + data.value + "\"></span>\n        </div>";
    },

    "font-size": function fontSize(data) {
        return "\n        <div class=\"data\" data-name=\"$" + data.name + "-font-size\">\n            <span style=\"font-size:" + data.value + "\" data-value=\"" + data.value + "\">Design System</span>\n        </div>";
    },

    codeView: function codeView(html) {
        return "\n            <div class=\"code-viewer\"></div>\n            <div class=\"--code\">\n                <pre>" + html + "</pre>\n            </div>\n        ";
    },

    indexItem: function indexItem(name) {
        return "\n        <li class=\"index-item\">\n            <a href=\"#" + name + "\">" + name + "</a>\n        </li>";
    },

    tokenBlock: function tokenBlock(data) {
        return "\n            <div class=\"section-content\">" + data.tpl + "</div>\n        ";
    },

    variableItem: function variableItem(data) {
        return "<div class=\"item\">\n            <span class=\"name\">" + data.variable + "</span>\n            <span class=\"value\">" + data.compiledValue + "</span>\n        </div>";
    },

    mixinItem: function mixinItem(data) {
        return "<div class=\"item\">\n            <span class=\"name\">" + data.name + "</span>\n            <span class=\"parameters\">" + data.parameter + "</span>\n        </div>";
    },

    groupTableBlock: function groupTableBlock(data) {
        return "\n            <strong class=\"section-sub-heading\">" + data.group + "</strong>\n            <div class=\"table-group\">" + data.tpl + "</div>\n        ";
    },

    tableDataBlock: function tableDataBlock(data) {
        return "<div class=\"table-group\">" + data.tpl + "</div>";
    }

};

},{}]},{},[1])
