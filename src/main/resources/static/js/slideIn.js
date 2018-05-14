(function(){ 
	if( $('.js-cd-panel-trigger').length > 0 ) {
		$('.js-cd-panel-trigger').each(function(i){
			var panelClass = '.js-cd-panel-'+ $(this).attr('data-panel'),
				panel = $(panelClass).first();
			// open panel when clicking on trigger btn
			$(this).on('click', function(event){
				event.preventDefault();
				panel.addClass('cd-panel--is-visible');
			});
			//close panel when clicking on 'x' or outside the panel
			panel.on('click', function(event){
				if( $(event.target).hasClass('js-cd-close') || $(event.target).hasClass(panelClass)) {
					panel.removeClass('cd-panel--is-visible');
				}
			});
		});
	}
})();