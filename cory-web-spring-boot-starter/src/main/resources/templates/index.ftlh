<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta name="_csrf" content="">
        <meta name="__form_token__" content="">

        <script type="text/javascript">
            <#-- 一些从后台初始化的js数据可以放这里 -->
            window.domainName = '${domainName!""}';
            window.ctx = '${ctx!""}';
            window.ctxWithoutSlash = '${ctxWithoutSlash!""}';
            window.successUrl = '${successUrl!""}';
            window.staticDir = '${staticDir!""}';
            window.jsFile = '${jsFile!""}';
            window.cssFile = '${cssFile!""}';
            window.errorPage = ${errorPage!"false"};
            window.errorType = '${errorType!""}';
            window.routerBase = '${ctxWithoutSlash!""}' + "/";
            window.publicPath = window.staticDir;
            window.DEBUG_MODE = ${debugMode!"false"};
            window.registerEnable = ${registerEnable!"false"};
        </script>

        <title>${siteName!""}<#if siteSlogan??> &#8211; ${siteSlogan!""}</#if></title>
        <meta name="keywords" content="${siteKeywords!""}">
        <meta name="description" content="${siteDescription!""}">

        <link rel="shortcut icon" href="${staticDir!""}favicon.ico" type="image/x-icon" />
        <link rel="icon" href="${staticDir!""}favicon.ico" type="image/x-icon" />

        <link rel="stylesheet" href="${staticDir!""}${cssFile!""}" />
    </head>
    <body>
        <div id="site-description-body">${siteDescriptionBody!""}</div>
        <script type="text/javascript">
            document.getElementById('site-description-body').remove();
        </script>
        <#if user??>
        <input type="hidden" id="current-user" value='${user}' />
        <script type="text/javascript" id="current-user-script">
            const userJson = document.getElementById('current-user').value;
            window.USER = JSON.parse(userJson);
            document.getElementById('current-user').remove();
            document.getElementById('current-user-script').remove();
        </script>
        </#if>
        <noscript>请打开浏览器Script功能.</noscript>
        <div id="root">
            <div style="margin-top: 50px; text-align: center; line-height: 20px; height: 20px; font-size: 12px; vertical-align: middle;">
                加载中...
                <img src="${staticDir!""}loading.gif" />
            </div>
        </div>

        <script src="${staticDir!""}${jsFile!""}"></script>
    </body>
</html>
