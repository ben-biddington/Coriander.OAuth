require 'rake'

desc 'Generate HTML from readme'
task :readme_preview do
	require 'maruku'
	
	readme = 'README.markdown'
	
	fail("Error. There is no #{readme} in <#{pwd}>") unless File.exists?(readme)
	
	maruku = Maruku.new(File.read(readme), {:title => "Dynasty readme"})

	puts maruku.to_html_document
end
