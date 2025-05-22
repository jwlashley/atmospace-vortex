{
description = "Atmospace-Vortex Development Flake";

inputs.nixpkgs.url = "nixpkgs/24.11";

outputs = inputs:
let
	system = "x86_64-linux";
	pkgs = inputs.nixpkgs.legacyPackages.${system};
in {
	devShell.${system} = pkgs.mkShell rec {
		name = "java-shell";
		buildInputs = with pkgs; [ jdk21_headless ];

		shellHook = ''
			export JAVA_HOME=${pkgs.jdk21_headless}
			PATH="${pkgs.jdk21_headless}/bin:$PATH"
		'';
	};
   };
}
