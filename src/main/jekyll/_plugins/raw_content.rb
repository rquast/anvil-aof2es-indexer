module Jekyll

  class RawContent < Generator

    def generate(site)
      site.pages.each do |page|
        page.data['raw_content'] = page.content
      end
    end
  
  end

end