/*
Copyright (C) 2011 Michal Harakal and Michael Kroez, P. Lipp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS-IS" BASIS
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
        
*/

package wheelmap.org;

public class WheelMapException extends RuntimeException {

	private static final long serialVersionUID = 3369069078613751197L;

	public WheelMapException() {
	}

	public WheelMapException(String message) {
		super(message);
	}

	public WheelMapException(Throwable cause) {
		super(cause);
	}

	public WheelMapException(String message, Throwable cause) {
		super(message, cause);
	}

}
