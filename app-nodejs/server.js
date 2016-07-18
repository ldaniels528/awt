/**
 * AWT ScalaJS Production Server Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./awt-nodejs-fastopt.js");
    const facade = com.microsoft.awt.AWTServerJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();