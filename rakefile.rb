require 'rake'

desc 'Generates github-styled html from a markdown file and writes it to stdout'
task :preview_github_readme, :file do |_, args|
	fail("Missing file argument") if args.file.nil?
	fail("Missing file #{args.file}") unless File.exists?(args.file)
	puts inject_github to_html(args.file)
end

def inject_github(html_text)
	require 'nokogiri'

	doc = Nokogiri::HTML(html_text)
	body = doc.xpath('//html/body').first

	readme_div = Nokogiri::XML::Element.new('div', doc) do |node|
	   node['class'] = 'announce md'
	   node['id'] = 'readme'
	end

	div = Nokogiri::XML::Element.new('div', doc) do |node|
	   node['class'] = 'wikistyle'
	end

	body.children.each {|child| child.parent = div}
	body['style'] = 'padding:20px'
	body.add_child(readme_div)
	readme_div.add_child div

	doc.to_html
end

def to_html(markdown_file)
	require 'maruku'

	style_sheets = [
		'http://github.com/stylesheets/bundle_common.css',
		'http://github.com/stylesheets/bundle_github.css',
	]

	Maruku.new(File.read(markdown_file), {
		:title => 'readme',
		:css => style_sheets.join(' ')
	}).to_html_document
end