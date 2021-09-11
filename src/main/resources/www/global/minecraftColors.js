var ANSI_CSS_CLASSES = {
    "\u001b[0;30;22m": "BLACK",
    "\u001b[0;34;22m": "DARK_BLUE",
    "\u001b[0;32;22m": "DARK_GREEN",
    "\u001b[0;36;22m": "DARK_AQUA",
    "\u001b[0;31;22m": "DARK_RED",
    "\u001b[0;35;22m": "DARK_PURPLE",
    "\u001b[0;33;22m": "GOLD",
    "\u001b[0;37;22m": "GRAY",
    "\u001b[0;30;1m": "DARK_GRAY",
    "\u001b[0;34;1m": "BLUE",
    "\u001b[0;32;1m": "GREEN",
    "\u001b[0;36;1m": "AQUA",
    "\u001b[0;31;1m": "RED",
    "\u001b[0;35;1m": "LIGHT_PURPLE",
    "\u001b[0;33;1m": "YELLOW",
    "\u001b[0;37;1m": "WHITE",
    "\u001b[5m": "OBFUSCATED",
    "\u001b[21m": "BOLD",
    "\u001b[9m": "STRIKETHROUGH",
    "\u001b[4m": "UNDERLINE",
    "\u001b[3m": "ITALIC",
    "\u001b[m": "RESET",

    "�0": "BLACK",
    "�1": "DARK_BLUE",
    "�2": "DARK_GREEN",
    "�3": "DARK_AQUA",
    "�4": "DARK_RED",
    "�5": "DARK_PURPLE",
    "�6": "GOLD",
    "�7": "GRAY",
    "�8": "DARK_GRAY",
    "�9": "BLUE",
    "�a": "GREEN",
    "�b": "AQUA",
    "�c": "RED",
    "�d": "LIGHT_PURPLE",
    "�e": "YELLOW",
    "�f": "WHITE",
    "�k": "OBFUSCATED",
    "�l": "BOLD",
    "�m": "STRIKETHROUGH",
    "�n": "UNDERLINE",
    "�o": "ITALIC",
    "�r": "RESET",
}

var MINECRAFT_CSS_CLASSES = {
    "§0": "BLACK",
    "§1": "DARK_BLUE",
    "§2": "DARK_GREEN",
    "§3": "DARK_AQUA",
    "§4": "DARK_RED",
    "§5": "DARK_PURPLE",
    "§6": "GOLD",
    "§7": "GRAY",
    "§8": "DARK_GRAY",
    "§9": "BLUE",
    "§a": "GREEN",
    "§b": "AQUA",
    "§c": "RED",
    "§d": "LIGHT_PURPLE",
    "§e": "YELLOW",
    "§f": "WHITE",
    "§k": "OBFUSCATED",
    "§l": "BOLD",
    "§m": "STRIKETHROUGH",
    "§n": "UNDERLINE",
    "§o": "ITALIC",
    "§r": "RESET"
}

function ansiStringToHTMLString(input) {
    var OPENEND_DIVS = 0;
    for (key in ANSI_CSS_CLASSES) {
        while (input.includes(key)) {
            input = input.replace(key, '<div class="ANSI_TEXT ANSI_' + ANSI_CSS_CLASSES[key] + '">');
            OPENEND_DIVS++;
        }
    }
    input += "</div>".repeat(OPENEND_DIVS);
    return input;
}

function minecraftStringToHTMLString(input) {
    var OPENEND_DIVS = 0;
    for (key in MINECRAFT_CSS_CLASSES) {
        while (input.includes(key)) {
            input = input.replace(key, '<div class="ANSI_TEXT ANSI_' + MINECRAFT_CSS_CLASSES[key] + '">');
            OPENEND_DIVS++;
        }
    }
    input += "</div>".repeat(OPENEND_DIVS);
    return input;

}