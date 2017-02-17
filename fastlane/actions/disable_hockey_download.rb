module Fastlane
  module Actions
    class DisableHockeyDownloadAction < Action
      def self.run(params)

      UI.success("Starting disable download for the previous version.")

        command = []
        command << "curl"
        command += upload_options(params[:api_token])    
        command << upload_url(params)

        shell_command = command.join(' ')
        result = Helper.is_test? ? shell_command : `#{shell_command}`
        fail_on_error(result)
        result

      end

      def self.fail_on_error(result)
        if result.include?("error")
          raise "Server error, failed to update the version".red
        end
      end

      def self.upload_url(params)

        "https://rink.hockeyapp.net/api/2/apps/#{params[:public_identifier]}/app_versions/#{params[:version_id]}"

      end

      def self.upload_options(api_token)

        options = []
         options << "-X PUT"
         options << "-F 'status=1'"
         options << "-H 'X-HockeyAppToken:#{api_token}'"
         options

      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Disable Download for the previous version"
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :api_token,
                                       env_name: "FL_HOCKEY_API_TOKEN",
                                       description: "API Token for Hockey Access",
                                       optional: false),

          FastlaneCore::ConfigItem.new(key: :public_identifier,
                                       env_name: "FL_HOCKEY_PUBLIC_IDENTIFIER",
                                       description: "Public identifier of the app you are targeting",
                                       optional: true),

          FastlaneCore::ConfigItem.new(key: :version_id,
                                       env_name: "FL_HOCKEY_VERSION_IDENTIFIER",
                                       description: "Version of the app you are targeting",
                                       optional: true)

        ]
      end

      def self.authors
        ["WaelGabsi"]
      end

      def self.is_supported?(platform)
        [:ios, :mac, :android].include? platform
      end
    end
  end
end