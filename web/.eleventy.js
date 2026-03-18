module.exports = function (eleventyConfig) {
  // Pass static assets through to dist/ root unchanged
  // {"public": "."} maps public/* → dist/*  (fonts at /fonts/, favicon at /favicon.svg)
  eleventyConfig.addPassthroughCopy({ public: "." });
  eleventyConfig.addPassthroughCopy("styles");

  // Enable Nunjucks for templates
  eleventyConfig.setTemplateFormats(["html", "njk"]);

  return {
    dir: {
      output: "dist",
      includes: "_includes",
      data: "_data",
    },
    htmlTemplateEngine: "njk",
    markdownTemplateEngine: "njk",
  };
};
