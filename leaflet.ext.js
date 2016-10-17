/**
 * @fileoverview First try on externs
 * @externs
 */

var L = {};

/**
 * @return {L.Map}
 */
L.map = function () {};

/**
 * return {L.Layer}
 */
L.tileLayer = function () {};

/**
 * @constructor
 */
L.Map = function () {};
L.Map.prototype.addLayer = function() {}
L.Map.prototype.on = function() {}
L.Map.prototype.off = function() {}

/**
 * @return {L.Fake}
 */
L.Map.prototype.getPanes = function() {}

L.Fake = function () {};
/**
 * @type {HTMLElement}
 */
L.Fake.prototype.overlayPane;

/**
 * @constructor
 */
L.Layer = function () {};
L.Layer.prototype.addLayer = function() {};


L.DomUtil = {};

/**
 * @return {HTMLElement}
 */
L.DomUtil.create = function (tagName, className, container) {};
