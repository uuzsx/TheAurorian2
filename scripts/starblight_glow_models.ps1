# Keep vanilla cross positions, rotation, UV orientation and cutout; only split the flower's four centre texels.
function New-StarblightGlowCrossModel([string]$Texture, [switch]$CentreOnly) {
    $regions=[System.Collections.Generic.List[object[]]]::new()
    if ($CentreOnly) {
        foreach ($r in @(@(0,0,16,10,0),@(0,12,16,16,0),@(0,10,7,12,0),@(7,10,9,12,15),@(9,10,16,12,0))) { $regions.Add($r) }
    } else { $regions.Add(@(0,0,16,16,15)) }
    $elements=@(foreach ($r in $regions) {
        $u0,$v0,$u1,$v1,$light=$r
        $low=[Math]::Round(.8+.9*$u0,6);$high=[Math]::Round(.8+.9*$u1,6)
        $forward=@($u0,$v0,$u1,$v1);$backward=@((16-$u1),$v0,(16-$u0),$v1)
        foreach ($plane in 0..1) {
            $faces=if ($plane -eq 0) {
                @{north=@{uv=$backward;texture='#cross'};south=@{uv=$forward;texture='#cross'}}
            } else { @{west=@{uv=$forward;texture='#cross'};east=@{uv=$backward;texture='#cross'}} }
            @{
                from=if ($plane -eq 0) {@($low,(16-$v1),8)} else {@(8,(16-$v1),$low)}
                to=if ($plane -eq 0) {@($high,(16-$v0),8)} else {@(8,(16-$v0),$high)}
                rotation=@{origin=@(8,8,8);axis='y';angle=45;rescale=$true}
                shade=$false;light_emission=$light;faces=$faces
            }
        }
    })
    return @{parent='minecraft:block/cross';render_type='minecraft:cutout';ambientocclusion=$false;
        textures=@{cross=$Texture};elements=$elements}
}
