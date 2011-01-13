require 'rake'

desc 'Apply apache license to all source files'
task :license do 
	copyright = 'Copyright 2011 Ben Biddington'
	the_license = <<-END.gsub(/^\t+/, '')
		/*
		#{copyright}

		Licensed under the Apache License, Version 2.0 (the \"License\");
		you may not use this file except in compliance with the License.
		You may obtain a copy of the License at

		   http://www.apache.org/licenses/LICENSE-2.0

		Unless required by applicable law or agreed to in writing, software
		distributed under the License is distributed on an \"AS IS\" BASIS,
		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		See the License for the specific language governing permissions and
		limitations under the License.
		*/\r\n
	END
	
	skipped,added = 0,0
	
	Dir.glob("**/*.{scal,jav}a") do |file|
		the_first_two_lines = IO.readlines(file)
		
		unless the_first_two_lines[1] =~ /#{copyright}/
			content = File.read file
			File.open file, 'w' do |io|
				io.write the_license
				io.write content
				puts "wrote to #{file}"
			end
			added +=1
		else
			skipped +=1
		end
	end	
	
	total = skipped + added
	
	puts "Process (#{total}) files, added (#{added}), skipped (#{skipped})"
	
	#all_file_types = Dir.glob("**/*.*").sort.uniq
end

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